---
hide:
  - navigation
  - toc
---

<div class="pub-masthead">
  <div class="pub-badge">
    <span class="badge-dot"></span>
    <span>ENGINEERING JOURNAL &amp; ARCHITECTURAL COMPENDIUM</span>
  </div>
  <h1 class="pub-headline">Take My Order: Engineering Journal</h1>
  <p class="pub-subhead">
    Architectural deep dives into distributed systems engineering: SAGA orchestration, cross-tenancy cloud infrastructure, transactional outbox patterns, private VCN peering, and stateless IAM authorization.
  </p>
  <div class="pub-meta-bar">
    <div class="pub-author-pill">
      <img src="https://github.com/OzerBerkay.png" alt="Berkay Özer" class="avatar-ring-img" />
      <div class="author-details">
        <span class="author-name">Berkay Özer</span>
        <span class="author-title">Senior Backend Engineer</span>
      </div>
    </div>
    <div class="pub-stats">
      <span class="stat-item"><strong>33</strong> In-Depth Chapters</span>
      <span class="stat-divider">·</span>
      <span class="stat-item"><strong>90+</strong> Verified Endpoints</span>
      <span class="stat-divider">·</span>
      <span class="stat-item"><strong>7-Node</strong> Cluster Topology</span>
    </div>
  </div>
</div>

<div class="setup-quickbar">
  <span class="setup-quickbar-label">⚡ DIRECT RUNBOOKS:</span>
  <a href="installation/local-setup/" class="setup-quickbar-btn">💻 Local Setup →</a>
  <a href="installation/seven-node-cluster-setup/" class="setup-quickbar-btn">🌐 7-Node Prod (2x OCI) →</a>
  <a href="installation/hybrid-cloud-aws-oracle-setup/" class="setup-quickbar-btn">☁️ 4-Node Hybrid (OCI+AWS) →</a>
  <a href="installation/keycloak-setup/" class="setup-quickbar-btn">🔐 Keycloak Setup →</a>
</div>

<div class="pub-topics-filter">
  <span class="topic-pill active">All Stories</span>
  <a href="installation/local-setup/" class="topic-pill">💻 Local Setup</a>
  <a href="installation/seven-node-cluster-setup/" class="topic-pill">🌐 Prod Setup</a>
  <a href="#architecture-services" class="topic-pill">🏛️ Architecture &amp; Services</a>
  <a href="#distributed-systems" class="topic-pill">⚡ Distributed Systems</a>
  <a href="#security-iam" class="topic-pill">🛡️ Security &amp; IAM</a>
  <a href="#devops-case-studies" class="topic-pill">☁️ DevOps &amp; Case Studies</a>
</div>

<!-- FEATURED LEAD STORY (HERO CARD) -->
<div class="featured-story-card">
  <div class="featured-story-badge">
    <span class="star-icon">★</span>
    <span>FEATURED ARCHITECTURAL ESSAY · 15 MIN READ</span>
  </div>
  <h2 class="featured-story-title">
    <a href="devops/cross-tenancy-topology/">The 7-Node Cross-Tenancy Architecture: Escaping Cloud Limits with Private LPG VCN Peering</a>
  </h2>
  <p class="featured-story-excerpt">
    An exhaustive architectural investigation into orchestrating 8 OCPU / 48 GB RAM of native ARM64 compute across two independent Oracle Cloud tenancies. How we bypassed the AWS 1 GB RAM memory ceiling, established a private sub-0.4 ms Local Peering Gateway mesh (10.0.0.0/16 ↔ 10.1.0.0/16), and scheduled stateful Kafka, Keycloak, and Bare-Metal PostgreSQL 16 with zero external registry dependency.
  </p>
  <div class="featured-story-footer">
    <div class="featured-tags">
      <span class="card-tag">Cross-Tenancy</span>
      <span class="card-tag">Kubernetes K3s</span>
      <span class="card-tag">Local Peering Gateway</span>
      <span class="card-tag">Native ARM64</span>
    </div>
    <a href="devops/cross-tenancy-topology/" class="read-story-btn">Read Architectural Essay →</a>
  </div>
</div>

<!-- SECTION 1: ARCHITECTURE & MICROSERVICES (12 Cards = 4x3 Grid) -->
<div class="editorial-section" id="architecture-services">
  <div class="section-masthead">
    <h2 class="section-title">🏛️ Architecture &amp; Domain Microservices</h2>
    <p class="section-desc">Domain-Driven Design (DDD), Hexagonal ports-and-adapters, core engineering competencies, and domain service specifications.</p>
  </div>
  <div class="stories-grid">
    <a href="architecture/project-overview/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">EXECUTIVE SUMMARY</span>
          <span class="reading-time">15 min read</span>
        </div>
        <h3 class="story-card-title">Project Overview &amp; Architecture Guide</h3>
        <p class="story-card-excerpt">Comprehensive overview of the platform, multi-portal stakeholder planes, end-to-end order journey, and core engineering pillars.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · System Overview</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="architecture/system-design/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">ARCHITECTURE</span>
          <span class="reading-time">18 min read</span>
        </div>
        <h3 class="story-card-title">System Design &amp; Microservices Architecture</h3>
        <p class="story-card-excerpt">Complete inventory of all 5 services, physical node placement, synchronous/asynchronous IPC protocols, and schema-isolated PostgreSQL stores.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · System Topology</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="architecture/competencies/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">COMPETENCIES</span>
          <span class="reading-time">12 min read</span>
        </div>
        <h3 class="story-card-title">Key Engineering Competencies</h3>
        <p class="story-card-excerpt">Structured blueprint of the platform's distributed consistency, zero-trust security, observability, and cloud-native operational patterns.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Engineering Pillars</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="architecture/hexagonal-architecture/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">CLEAN CODE</span>
          <span class="reading-time">12 min read</span>
        </div>
        <h3 class="story-card-title">Clean &amp; Hexagonal Architecture (Ports and Adapters)</h3>
        <p class="story-card-excerpt">Strictly isolating framework-free Java 21 domain cores from JPA, Kafka, Keycloak, and REST adapters via explicit Maven module boundaries.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Software Design</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="architecture/domain-driven-design/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">DDD PATTERNS</span>
          <span class="reading-time">14 min read</span>
        </div>
        <h3 class="story-card-title">Domain-Driven Design (DDD) &amp; Bounded Contexts</h3>
        <p class="story-card-excerpt">Strategic subdomain classification, context maps, anti-corruption read replicas, rich aggregates, and self-validating domain invariants.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Domain Modeling</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="tech-stack/ecosystem/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">TECH STACK</span>
          <span class="reading-time">10 min read</span>
        </div>
        <h3 class="story-card-title">Technology Ecosystem &amp; Infrastructure Matrix</h3>
        <p class="story-card-excerpt">Exact Java 21, Spring Boot 3.4.5, Kafka, Avro, PostgreSQL 16, Keycloak 24.0.1, Redis, and K3s runtime matrix across local and production.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Tech Ecosystem</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="services/order-service/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">CORE ENGINE</span>
          <span class="reading-time">16 min read</span>
        </div>
        <h3 class="story-card-title">Order Service (Saga Orchestrator) Specification</h3>
        <p class="story-card-excerpt">Managing the 10-state order lifecycle, SAGA orchestration, kitchen fulfillment transitions, outbox dispatchers, and local catalog replicas.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Microservices</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="services/payment-service/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">FINANCIAL DOMAIN</span>
          <span class="reading-time">14 min read</span>
        </div>
        <h3 class="story-card-title">Payment Service: Wallets, Order Escrow &amp; Ledger</h3>
        <p class="story-card-excerpt">Customer and merchant wallets, per-order escrow custody (HOLDING → RELEASED / REFUNDED), immutable ledger writes, and compensating refunds.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Payment Engine</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="services/restaurant-service/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">CATALOG DOMAIN</span>
          <span class="reading-time">12 min read</span>
        </div>
        <h3 class="story-card-title">Restaurant Service: Catalog, Stock &amp; Personnel</h3>
        <p class="story-card-excerpt">Branch lifecycle, versioned menu categories, operating hours, personnel onboarding, validate-first stock reservation, and Kafka-synced ratings.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Catalog Engine</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="services/restaurant-rating-system/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">EVENT-DRIVEN</span>
          <span class="reading-time">10 min read</span>
        </div>
        <h3 class="story-card-title">Verified Restaurant Review &amp; Rating Engine</h3>
        <p class="story-card-excerpt">Bounded within order-service to avoid distributed 2PC locks, enforcing verified DELIVERED reviews and pushing LWW rating updates via Kafka.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Rating System</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="services/identity-service/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">IDENTITY DOMAIN</span>
          <span class="reading-time">11 min read</span>
        </div>
        <h3 class="story-card-title">Identity Service: Headless IAM, Profiles &amp; RBAC</h3>
        <p class="story-card-excerpt">Headless Keycloak OIDC facade, portal-segregated authentication, user profiles, delivery addresses, dynamic RBAC governance, and Redis revocation.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Identity Service</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="services/gateway-service/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">EDGE ROUTING</span>
          <span class="reading-time">9 min read</span>
        </div>
        <h3 class="story-card-title">Gateway Service: Spring Cloud Gateway &amp; Edge Security</h3>
        <p class="story-card-excerpt">Reactive WebFlux path routing, Split-Horizon OAuth2 JWT validation, global CORS enforcement, and centralized Read-Only Swagger UI aggregation.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · API Gateway</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>
  </div>
</div>

<!-- SECTION 2: DISTRIBUTED SYSTEMS (6 Cards = 2x3 Grid) -->
<div class="editorial-section" id="distributed-systems">
  <div class="section-masthead">
    <h2 class="section-title">⚡ Distributed Transactions &amp; SAGA Orchestration</h2>
    <p class="section-desc">Deterministic consistency guarantees, transactional outbox idempotency, and parallel Fork-Join compensation across Kafka boundaries.</p>
  </div>
  <div class="stories-grid">
    <a href="distributed-systems/saga-orchestration/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">SAGA ORCHESTRATION</span>
          <span class="reading-time">15 min read</span>
        </div>
        <h3 class="story-card-title">The 10-State Order Lifecycle &amp; SAGA Orchestration</h3>
        <p class="story-card-excerpt">Complete breakdown of order states from PENDING to DELIVERED or CANCELLED, orchestrated forward steps, and Fork-Join compensating rollbacks.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Distributed Systems</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="distributed-systems/cancel-saga-deep-dive/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">FORENSIC ANALYSIS</span>
          <span class="reading-time">25 min read</span>
        </div>
        <h3 class="story-card-title">Cancel SAGA &amp; In-Place Outbox Architecture</h3>
        <p class="story-card-excerpt">Autopsy of the NonUniqueResultException deadlock, eliminating concurrency race conditions, and enforcing the Outbox Single-Row Rule.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Forensic Deep-Dive</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="distributed-systems/transactional-outbox/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">DATA PATTERNS</span>
          <span class="reading-time">12 min read</span>
        </div>
        <h3 class="story-card-title">Transactional Outbox &amp; Concurrent Polling</h3>
        <p class="story-card-excerpt">Eliminating dual-write hazards without 2PC using PostgreSQL SELECT FOR UPDATE SKIP LOCKED and atomic in-place outbox transitions.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Event-Driven</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="distributed-systems/escrow-and-settlement/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">FINANCIAL LEDGER</span>
          <span class="reading-time">14 min read</span>
        </div>
        <h3 class="story-card-title">Order Escrow Pool &amp; Settlement Choreography</h3>
        <p class="story-card-excerpt">Per-order financial custody isolating captured funds in HOLDING under row-level pessimistic locking until delivery settlement or refund.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Financial Systems</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="distributed-systems/idempotency-and-messaging/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">MESSAGING</span>
          <span class="reading-time">11 min read</span>
        </div>
        <h3 class="story-card-title">Idempotency &amp; Message Ordering Architecture</h3>
        <p class="story-card-excerpt">Enforcing entity partition keys in Apache Kafka, multi-layer consumer idempotency, LWW zombie prevention, and HTTP Idempotency-Key deduplication.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Kafka Architecture</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="distributed-systems/avro-contracts/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">EVENT SCHEMAS</span>
          <span class="reading-time">10 min read</span>
        </div>
        <h3 class="story-card-title">Avro Event Schemas &amp; Contract Governance</h3>
        <p class="story-card-excerpt">Strongly typed Apache Avro contracts across 12 Kafka topics, Confluent Schema Registry governance, and backwards-compatible evolution.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Schema Registry</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>
  </div>
</div>

<!-- SECTION 3: SECURITY & IAM (6 Cards = 2x3 Grid) -->
<div class="editorial-section" id="security-iam">
  <div class="section-masthead">
    <h2 class="section-title">🛡️ Enterprise Security, Identity &amp; Zero-Trust Governance</h2>
    <p class="section-desc">Sub-millisecond authorization, role-bound tenant isolation, multi-tier RBAC, and crash-safe user synchronization.</p>
  </div>
  <div class="stories-grid">
    <a href="security/stateless-idor-shield/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">ZERO-TRUST</span>
          <span class="reading-time">10 min read</span>
        </div>
        <h3 class="story-card-title">Stateless Zero-SQL IDOR Shield Architecture</h3>
        <p class="story-card-excerpt">Same-role bound tenant validation combining JWT role_ids with L1 Caffeine-cached role replicas to block cross-branch IDOR without per-request SQL.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Security</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="security/keycloak-architecture/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">IAM INFRASTRUCTURE</span>
          <span class="reading-time">13 min read</span>
        </div>
        <h3 class="story-card-title">Keycloak OIDC Integration &amp; Token Architecture</h3>
        <p class="story-card-excerpt">Headless OIDC auth proxy, Split-Horizon cached JWKS verification, 5 custom RS256 claims, compensating sign-up rollback, and M2M service accounts.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Identity</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="security/rbac-governance/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">ACCESS CONTROL</span>
          <span class="reading-time">12 min read</span>
        </div>
        <h3 class="story-card-title">Multi-Tier RBAC Governance &amp; Permission Modeling</h3>
        <p class="story-card-excerpt">Separating CUSTOMER, MERCHANT, INTERNAL, and M2M tiers with subset delegation guards, domain-filtered Kafka replicas, and O(1) role revocation.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Governance</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="security/token-lifecycle-and-revocation/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">TOKEN MANAGEMENT</span>
          <span class="reading-time">11 min read</span>
        </div>
        <h3 class="story-card-title">Token Lifecycle &amp; Multi-Tier Redis Revocation</h3>
        <p class="story-card-excerpt">5-minute RS256 access tokens, non-rotating 7d/30d SPA sessions, batched Redis MGET checks, and instant session, user, or role-level invalidation.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Session Security</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="security/user-update-engine/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">DATA INTEGRITY</span>
          <span class="reading-time">12 min read</span>
        </div>
        <h3 class="story-card-title">Crash-Safe Distributed User Update Engine</h3>
        <p class="story-card-excerpt">Intent-driven dual-store reconciliation synchronizing Keycloak Admin API and PostgreSQL without holding DB transactions over HTTP calls.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Data Consistency</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="architecture/subdomain-and-portals/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">PORTAL TOPOLOGY</span>
          <span class="reading-time">10 min read</span>
        </div>
        <h3 class="story-card-title">Subdomain Topology &amp; Cloudflare Edge Routing</h3>
        <p class="story-card-excerpt">Traffic segmentation across 7 role-isolated subdomains—3 Angular portals, API Gateway, Keycloak IAM, Grafana, and Jenkins—via Cloudflare Origin Rules.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Edge Architecture</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>
  </div>
</div>

<!-- SECTION 4: SETUP RUNBOOKS, CLOUD, DEVOPS & CASE STUDIES (9 Cards = 3x3 Grid) -->
<div class="editorial-section" id="devops-case-studies">
  <div class="section-masthead">
    <h2 class="section-title">☁️ Setup Runbooks, Cloud DevOps &amp; Case Studies</h2>
    <p class="section-desc">Step-by-step local and multi-cloud provisioning runbooks, CI/CD pipelines, autoscaling, architectural decisions, and forensic post-mortems.</p>
  </div>
  <div class="stories-grid">
    <a href="installation/local-setup/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">LOCAL RUNBOOK</span>
          <span class="reading-time">18 min read</span>
        </div>
        <h3 class="story-card-title">Local Development Setup (Mode A &amp; Mode B)</h3>
        <p class="story-card-excerpt">Complete workstation guide covering Mode A (Bare-Metal PostgreSQL + Docker Compose + IDE) and Mode B (full local K8s overlays/dev cluster).</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Local Setup</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="installation/seven-node-cluster-setup/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">PROD RUNBOOK</span>
          <span class="reading-time">25 min read</span>
        </div>
        <h3 class="story-card-title">Production 7-Node Cluster Setup (2x OCI)</h3>
        <p class="story-card-excerpt">Click-by-click provisioning of 2x Oracle Cloud tenancies, LPG peering, Bare-Metal PostgreSQL 16, K3s, native ARM64 Jenkins, and Cloudflare HTTPS.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · 7-Node OCI</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="installation/hybrid-cloud-aws-oracle-setup/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">ALT PROD RUNBOOK</span>
          <span class="reading-time">20 min read</span>
        </div>
        <h3 class="story-card-title">Alternative 4-Node Hybrid Setup (OCI + AWS)</h3>
        <p class="story-card-excerpt">End-to-end deployment across 1x OCI ARM64 account and 1x AWS t2.micro x86_64 edge node using Flannel VXLAN, Docker Hub, and GitHub Actions.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Hybrid Cloud</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="installation/keycloak-setup/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">IAM RUNBOOK</span>
          <span class="reading-time">15 min read</span>
        </div>
        <h3 class="story-card-title">Keycloak Realm Configuration (24.0.1)</h3>
        <p class="story-card-excerpt">Both 1-click JSON import and click-by-click manual setup of take-my-order-realm, User Profile attributes, 5 JWT mappers, and 7d/30d sessions.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Keycloak IAM</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="devops/cross-tenancy-topology/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">NETWORKING MESH</span>
          <span class="reading-time">15 min read</span>
        </div>
        <h3 class="story-card-title">Cross-Tenancy Infrastructure &amp; LPG Mesh</h3>
        <p class="story-card-excerpt">Bridging two Oracle Cloud tenancies via Local Peering Gateways with route tables, security lists, nodeSelector placement, and sub-0.4 ms ping.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · OCI Cloud</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="devops/jenkins-ci-cd-pipeline/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">CI/CD AUTOMATION</span>
          <span class="reading-time">14 min read</span>
        </div>
        <h3 class="story-card-title">CI/CD Pipelines: Native ARM64 Jenkins &amp; GitHub Actions</h3>
        <p class="story-card-excerpt">Comparing native ARM64 Jenkins SSH image streaming into K3s containerd with QEMU/Buildx multi-arch GitHub Actions delivery via Docker Hub.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · CI/CD Pipelines</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="devops/hpa-and-autoscaling/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">AUTOSCALING</span>
          <span class="reading-time">11 min read</span>
        </div>
        <h3 class="story-card-title">Horizontal Pod Autoscaling (HPA) &amp; Anti-Affinity</h3>
        <p class="story-card-excerpt">CPU-driven autoscaling (70% threshold), podAntiAffinity spreading across workers, Kafka partition alignment, and zero-downtime rolling updates.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Kubernetes</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="devops/architectural-decision-records/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">GOVERNANCE</span>
          <span class="reading-time">16 min read</span>
        </div>
        <h3 class="story-card-title">Architectural Decision Records (ADRs): The 8 Decisions</h3>
        <p class="story-card-excerpt">Engineering rationale behind 2x OCI LPG peering, per-order escrow, In-Place Outbox, Caffeine IDOR, native ARM64 Jenkins, Avro, and O(1) Redis revocation.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Architecture ADRs</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>

    <a href="case-studies/critical-incidents/" class="story-card">
      <div>
        <div class="story-card-header">
          <span class="card-tag">FORENSIC POST-MORTEM</span>
          <span class="reading-time">22 min read</span>
        </div>
        <h3 class="story-card-title">Production Post-Mortems &amp; Critical Incidents</h3>
        <p class="story-card-excerpt">Seven root-cause analyses covering Caffeine cache collisions, SAGA Fork-Join deadlocks, Kafka Cluster ID mismatches, HikariCP starvation, and Alpine JNI crashes.</p>
      </div>
      <div class="story-card-footer">
        <span>Berkay Özer · Post-Mortem</span>
        <span class="story-arrow">Read →</span>
      </div>
    </a>
  </div>
</div>

<!-- MEDIUM / SUBSTACK SIGNATURE AUTHOR BIO CARD -->
<div class="pub-author-card">
  <img src="https://github.com/OzerBerkay.png" alt="Berkay Özer" class="author-card-avatar-img" />
  <div class="author-card-body">
    <div class="author-card-header">
      <div>
        <h3 class="author-card-name">Berkay Özer</h3>
        <p class="author-card-role">Senior Backend Engineer</p>
      </div>
      <a href="https://github.com/OzerBerkay/take-my-order" target="_blank" class="author-card-follow-btn">View on GitHub ↗</a>
    </div>
    <p class="author-card-bio">
      Author and architect of the <strong>Take My Order</strong> distributed microservices ecosystem. Specializing in high-throughput event-driven systems, SAGA orchestration consistency, cross-tenancy cloud infrastructure, zero-trust IAM security, and resilient software architectures.
    </p>
    <div class="author-card-badges">
      <span class="bio-tag">Java 21</span>
      <span class="bio-tag">Spring Boot 3.4+</span>
      <span class="bio-tag">Kubernetes (K3s)</span>
      <span class="bio-tag">Apache Kafka</span>
      <span class="bio-tag">Keycloak 24+</span>
      <span class="bio-tag">Oracle Cloud (OCI)</span>
      <span class="bio-tag">Jenkins ARM64 CI/CD</span>
      <span class="bio-tag">Transactional Outbox</span>
      <span class="bio-tag">Hexagonal Architecture</span>
    </div>
  </div>
</div>
