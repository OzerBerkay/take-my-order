# Order Escrow Pool & Settlement Choreography

## 1. Financial Custody Motivation

In naive food ordering systems, capturing customer payment (`OrderStatus.PAID`) immediately credits the restaurant's withdrawable wallet (`restaurantWallet.addBalance`). At that point, however, the kitchen has not yet prepared the meal and the courier has not completed delivery.

Crediting merchants prematurely introduces two critical architectural hazards:

1. **Early Withdrawal & Negative Balance Risk:** If a merchant withdraws funds while orders are still active and one of those orders is cancelled, SAGA refund compensation (`cancelPayment`) either fails with `PaymentDomainException` or forces the merchant wallet into a negative balance.
2. **Distributed Monolith Coupling:** Blocking withdrawals by having `payment-service` synchronously query `order-service` for active order totals couples runtime availability and violates bounded-context autonomy.

**Take My Order** resolves both hazards by holding captured funds inside a lock-free **Mini Order Escrow Pool (`payment.order_escrows`)** until physical handover (`DELIVERED`), and releasing earnings to the merchant via asynchronous, SAGA-independent **Settlement Choreography (`order-delivered`)**.

---

## 2. Per-Order Escrow Design Principles

Instead of funneling all in-flight order funds through a single `OwnerType.PLATFORM` wallet row, each paid order creates its own isolated row in `payment.order_escrows`:

```mermaid
flowchart LR
    subgraph AntiPattern ["Naive Anti-Pattern: Single Platform Wallet Row (Rejected)"]
        direction LR
        OrdersA["Concurrent Order<br/>Payments & Refunds"] --> PlatWallet[("Single Platform Wallet Row<br/>SELECT ... FOR UPDATE")]
        PlatWallet --- Contention["Global Hotspot Contention:<br/>Every checkout, refund & delivery<br/>blocks on the exact same row"]
    end

    subgraph ImplementedPattern ["Implemented Pattern: Per-Order Escrow Rows (Lock-Free)"]
        direction LR
        OrdersB["Concurrent Order<br/>Payments"] --> EscrowRows[("payment.order_escrows<br/>Isolated Row per order_id<br/>(HOLDING)")]
        EscrowRows --- Benefits["Zero Cross-Order Lock Contention<br/>Per-Order Audit Trail & T+N Ready"]
    end
```

- **Zero Hotspot Contention:** Concurrent checkouts across thousands of customers and restaurants insert independent escrow rows (`UNIQUE (order_id)`) without contending on a shared global balance row.
- **Strict SAGA Boundary Separation:** Payment capture and cancellation refund stay inside the orchestrated Order SAGA (`payment-request` / `payment-response`), whereas settlement upon delivery is a one-way post-terminal choreography event (`order-delivered`).
- **Autonomous Merchant Payouts:** Because restaurant wallets only receive funds once orders reach `DELIVERED`, withdrawal validation remains 100% local to `payment-service`.

---

## 3. Persistence Model & State Semantics

All escrow, wallet, and delivery outbox tables are managed via Flyway migration scripts across `payment-service` and `order-service`:

```mermaid
erDiagram
    PAYMENT_ORDER_ESCROWS ||--o| PAYMENT_WALLETS : "credits restaurant on RELEASED"
    PAYMENT_WALLETS ||--o{ PAYMENT_WALLET_TRANSACTIONS : "immutable ledger"
    ORDER_DELIVERY_OUTBOX ||..o| PAYMENT_ORDER_ESCROWS : "order-delivered triggers release"

    PAYMENT_ORDER_ESCROWS {
        uuid id PK
        uuid order_id UK
        uuid restaurant_id
        uuid customer_id
        numeric amount
        varchar status "HOLDING | RELEASED | REFUNDED"
        timestamptz created_at
        timestamptz updated_at
        int version "Optimistic Lock"
    }

    PAYMENT_WALLETS {
        uuid id PK
        uuid owner_id UK
        varchar owner_type "CUSTOMER | RESTAURANT"
        numeric balance
        int version
    }

    PAYMENT_WALLET_TRANSACTIONS {
        uuid id PK
        uuid wallet_id FK
        numeric amount
        varchar transaction_type "PAYMENT | REFUND | ORDER_PAYOUT | DEPOSIT | WITHDRAWAL"
        varchar reference_id
        varchar idempotency_key UK
    }

    ORDER_DELIVERY_OUTBOX {
        uuid id PK
        uuid order_id
        varchar type "OrderDelivery"
        varchar outbox_status "STARTED | COMPLETED | FAILED"
        int version
    }
```

### 3.1 Escrow Lifecycle & Ledger Mapping

| Entity / Escrow State | Schema, Locks & Indexes | Domain & Ledger Semantics |
| :--- | :--- | :--- |
| **`payment.order_escrows`**<br/>(`OrderEscrow`) | `UNIQUE (order_id)`, `@Version` optimistic lock, `PESSIMISTIC_WRITE` (`findByOrderIdWithLock`), indexes on `(restaurant_id, status)` and `(created_at)`. | Enforces strict one-to-one custody per order; `OrderEscrow` `release()` and `refund()` guard `status == HOLDING` (`PaymentDomainException` otherwise). |
| **`HOLDING`** | Created atomically inside `PaymentDomainServiceImpl` `validateAndInitiatePayment()`. | Customer wallet debited (`PAYMENT` ledger entry); funds held in escrow while merchant wallet stays untouched. |
| **`RELEASED`** | Updated under `PESSIMISTIC_WRITE` inside `settleOrder()` (`OrderDeliveredMessageListener`). | Locks restaurant wallet (`findByOwnerIdWithLock`), lazily creates a `RestaurantWallet` if absent, credits earnings, and records `ORDER_PAYOUT`. |
| **`REFUNDED`** | Updated inside `PaymentDomainServiceImpl` `validateAndCancelPayment()`. | Returns funds directly from escrow to customer wallet and logs `REFUND`; merchant wallet is never debited. |
| **`"order".delivery_outbox`** | Persisted inside `OrderDeliverCommandHandler` (`type = "OrderDelivery"`, `STARTED`). | Polled every `10s` by `DeliveryOutboxScheduler` to publish `OrderDeliveredAvroModel` on `order-delivered`, preventing lost settlements. |

---

## 4. End-to-End Financial & Settlement Flows

### 4.1 Order Payment Capture (`HOLDING`)

```mermaid
sequenceDiagram
    autonumber
    participant OS as order-service
    participant PS as payment-service
    participant CW as customer_wallet
    participant OE as payment.order_escrows
    participant RW as restaurant_wallet

    OS->>PS: Kafka: payment-request (PENDING)
    PS->>CW: subtractBalance(price) + log(PAYMENT)
    Note over PS,RW: Restaurant wallet remains untouched
    PS->>OE: INSERT order_escrows (status = HOLDING)
    PS-->>OS: Kafka: payment-response (COMPLETED)
```

### 4.2 Cancellation Refund (`REFUNDED`)

```mermaid
sequenceDiagram
    autonumber
    participant OS as order-service
    participant PS as payment-service
    participant OE as payment.order_escrows
    participant CW as customer_wallet
    participant RW as restaurant_wallet

    OS->>PS: Kafka: payment-request (CANCELLED)
    PS->>OE: findByOrderId(orderId) -> refund() (HOLDING -> REFUNDED)
    PS->>CW: addBalance(price) + log(REFUND)
    Note over PS,RW: Restaurant wallet untouched (zero negative-balance risk)
    PS-->>OS: Kafka: payment-response (CANCELLED)
```

### 4.3 Delivery Settlement (`RELEASED`)

When a courier confirms physical delivery (`POST /orders/{orderId}/deliver`), `OrderDeliverCommandHandler` transitions the order to `DELIVERED`, seals the SAGA outboxes as `SagaStatus.SUCCEEDED`, and inserts a `delivery_outbox` record (`type = "OrderDelivery"`) in the exact same database transaction:

```mermaid
sequenceDiagram
    autonumber
    participant OS as order-service
    participant DO as "order".delivery_outbox
    participant K as Kafka (order-delivered)
    participant PS as payment-service
    participant OE as payment.order_escrows
    participant RW as restaurant_wallet

    Note over OS: POST /orders/{orderId}/deliver -> DELIVERED + SagaStatus.SUCCEEDED
    OS->>DO: INSERT delivery_outbox (type = OrderDelivery, STARTED)
    DO->>K: DeliveryOutboxScheduler -> OrderDeliveredAvroModel
    K->>PS: OrderDeliveredKafkaListener -> settleOrder()
    PS->>OE: findByOrderIdWithLock (PESSIMISTIC_WRITE)
    alt status == HOLDING
        PS->>OE: release() -> UPDATE status = RELEASED
        PS->>RW: findByOwnerIdWithLock (create if absent) + addBalance(amount)
        PS->>PS: INSERT wallet_transactions (ORDER_PAYOUT)
    else status == RELEASED (Duplicate Redelivery)
        Note over PS: Idempotent NO-OP (zero double payout)
    else status == REFUNDED (Already Cancelled)
        Note over PS: Warn & skip (cannot release refunded escrow)
    end
```

### 4.4 Autonomous Merchant Withdrawal

Because `payment.wallets.balance` for a restaurant holds **only** settled earnings (`ORDER_PAYOUT`), `POST /wallets/{ownerId}/withdraw` (`WalletController`) executes completely autonomously inside `payment-service`:

- Enforces restaurant-scoped RBAC (`can_manage_payment` via `PaymentAuthService.hasPermissionForWallet`) and requires an `Idempotency-Key` header persisted on `wallet_transactions`.
- Acquires a `PESSIMISTIC_WRITE` wallet lock (`walletRepository.findByOwnerIdWithLock(ownerId)`), verifies sufficient local balance via `wallet.subtractBalance()`, and logs `WalletTransactionType.WITHDRAWAL` with zero runtime dependency on `order-service`.

### 4.5 Restaurant Financial Summary API

To maintain strict bounded-context separation, operational counts remain in `order-service` (`GET /orders/restaurants/{restaurantId}/summary`), while monetary telemetry lives in `payment-service`:

- **Endpoint:** `GET /wallets/{ownerId}/financial-summary`
- **Authorization:** `@PreAuthorize("@paymentAuthService.hasPermissionForWallet(authentication, 'can_read_payment', #ownerId)")` — applying the exact same IDOR shield as `/balance` and `/history`.
- **Response (`RestaurantFinancialSummaryResponse`):** Aggregates wallet balance plus a single native `FILTER` query in `OrderEscrowJpaRepository.getEscrowSummary()` anchored to UTC `startOfDay`:
    - `restaurantId`: Target restaurant owner UUID.
    - `withdrawableBalance`: Available restaurant wallet balance (`payment.wallets.balance`).
    - `dailySettledRevenue`: Sum of escrows with `status = 'RELEASED'` and `updated_at >= startOfDay (UTC)`.
    - `pendingPayout`: Sum of active escrows currently in `status = 'HOLDING'`.
    - `dailyRefundedAmount`: Sum of escrows with `status = 'REFUNDED'` and `updated_at >= startOfDay (UTC)`.

---

## 5. Solvency Custody & Reconciliation

Dedicated per-order escrow records allow finance and operations teams to reconcile platform liabilities cleanly using indexed aggregates over `payment.order_escrows`:

| Custody / Audit Metric | Escrow Filter & Index Path | Operational Guarantee |
| :--- | :--- | :--- |
| **Platform Escrow Liability (`Pending Payout`)** | `status = 'HOLDING'` (per-restaurant via `idx_order_escrows_restaurant_status`) | Real-time custody balance across all active orders, proving full backing for in-flight refunds. |
| **Daily Settled Revenue** | `status = 'RELEASED'` and `updated_at >= startOfDay (UTC)` | Reconciles directly against `ORDER_PAYOUT` credits posted to merchant wallets. |
| **Daily Refunded Volume** | `status = 'REFUNDED'` and `updated_at >= startOfDay (UTC)` | Reconciles directly against `REFUND` credits returned to customer wallets. |
| **`T+N` Rolling Reserve Readiness** | `created_at` & `updated_at` timestamps (`idx_order_escrows_created_at`) | Supports future delayed-payout or dispute-holdback policies without modifying table schemas. |

### 5.1 Related Documentation

- [SAGA Orchestration Pattern](saga-orchestration.md) — Order lifecycle states, payment capture, and cancellation triggers.
- [Cancel SAGA Deep Dive](cancel-saga-deep-dive.md) — Refund compensation legs and zero-Kafka no-refund delivery edge cases.
- [Transactional Outbox Pattern](transactional-outbox.md) — Reliable delivery outbox scheduling and at-least-once semantics.
- [Idempotency & Messaging](idempotency-and-messaging.md) — Consumer deduplication and ledger uniqueness constraints.
- Service Specifications: [Payment Service](../services/payment-service.md) · [Order Service](../services/order-service.md)
