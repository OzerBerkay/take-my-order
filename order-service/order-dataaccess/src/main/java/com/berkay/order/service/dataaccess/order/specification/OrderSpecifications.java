package com.berkay.order.service.dataaccess.order.specification;

import com.berkay.domain.valueobject.CancelReason;
import com.berkay.domain.valueobject.OrderStatus;
import com.berkay.order.service.dataaccess.order.entity.OrderAddressEntity;
import com.berkay.order.service.dataaccess.order.entity.OrderEntity;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderSpecifications {

    public static Specification<OrderEntity> createRestaurantFilter(
            UUID restaurantId,
            OrderStatus status,
            String cleanSearch,
            List<OrderStatus> darkAreaStatuses,
            String sort) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Coklu Kiracilik (Multi-Tenancy) & Restoran Izolasyonu
            predicates.add(cb.equal(root.get("restaurantId"), restaurantId));

            // 2. Karanlik Alan Kalkani 1 (PENDING ve PAID Asla Restorana Gosterilmez)
            if (darkAreaStatuses != null && !darkAreaStatuses.isEmpty()) {
                predicates.add(cb.not(root.get("orderStatus").in(darkAreaStatuses)));
            }

            // 3. Karanlik Alan Kalkani 2 (PAYMENT_FAILED Iptalleri Asla Gosterilmez)
            predicates.add(cb.or(
                    cb.isNull(root.get("cancelReason")),
                    cb.notEqual(root.get("cancelReason"), CancelReason.PAYMENT_FAILED)
            ));

            // 4. Dinamik Statu Sekmesi Filtresi
            if (status != null) {
                predicates.add(cb.equal(root.get("orderStatus"), status));
            }

            // 5. Akilli Arama Filtresi (Yalnizca arama terimi girildiyse calisir)
            if (cleanSearch != null && !cleanSearch.trim().isEmpty()) {
                Join<OrderEntity, OrderAddressEntity> address = root.join("address", JoinType.LEFT);
                String pattern = "%" + cleanSearch.toLowerCase().trim() + "%";

                // A. Ad-Soyad Bosluksuz Birlesik Arama
                Expression<String> fullName = cb.concat(
                        cb.coalesce(address.get("contactFirstName"), ""),
                        cb.coalesce(address.get("contactLastName"), "")
                );
                Expression<String> cleanFullName = cb.function("replace", String.class, fullName, cb.literal(" "), cb.literal(""));
                Predicate namePredicate = cb.like(cb.lower(cleanFullName), pattern);

                // B. Telefon Arama
                Predicate phonePredicate = cb.like(address.get("contactPhone"), "%" + cleanSearch.trim() + "%");

                // C. Tracking ID Arama
                Expression<String> trackingIdStr = cb.function("text", String.class, root.get("trackingId"));
                Predicate trackingPredicate = cb.like(trackingIdStr, pattern);

                predicates.add(cb.or(namePredicate, phonePredicate, trackingPredicate));
            }

            // 6. Akilli Operasyonel Hiyerarsi Siralamasi (CASE WHEN)
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                applySorting(root, query, cb, status, sort);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void applySorting(Root<OrderEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb,
                                     OrderStatus status, String sort) {
        jakarta.persistence.criteria.Order secondaryOrder = resolveSecondaryOrder(root, cb, sort);

        if (status != null) {
            query.orderBy(secondaryOrder);
        } else {
            Expression<Integer> statusOrder = cb.<Integer>selectCase()
                    .when(cb.equal(root.get("orderStatus"), OrderStatus.WAITING_FOR_APPROVAL), 1)
                    .when(cb.equal(root.get("orderStatus"), OrderStatus.APPROVED), 2)
                    .when(cb.equal(root.get("orderStatus"), OrderStatus.PREPARING), 3)
                    .when(cb.equal(root.get("orderStatus"), OrderStatus.READY_FOR_PICKUP), 4)
                    .when(cb.equal(root.get("orderStatus"), OrderStatus.IN_DELIVERY), 5)
                    .when(cb.equal(root.get("orderStatus"), OrderStatus.CANCELLING), 6)
                    .when(cb.equal(root.get("orderStatus"), OrderStatus.DELIVERED), 7)
                    .when(cb.equal(root.get("orderStatus"), OrderStatus.CANCELLED), 8)
                    .otherwise(9);

            query.orderBy(cb.asc(statusOrder), secondaryOrder);
        }
    }

    private static jakarta.persistence.criteria.Order resolveSecondaryOrder(Root<OrderEntity> root, CriteriaBuilder cb, String sort) {
        if (sort != null) {
            String lower = sort.toLowerCase().trim();
            if (lower.equals("createdat,desc") || lower.equals("created_at,desc") || lower.equals("createdat:desc")) {
                return cb.desc(root.get("createdAt"));
            }
            if (lower.equals("price,desc") || lower.equals("price:desc")) {
                return cb.desc(root.get("price"));
            }
            if (lower.equals("price,asc") || lower.equals("price:asc")) {
                return cb.asc(root.get("price"));
            }
            if (lower.equals("createdat,asc") || lower.equals("created_at,asc") || lower.equals("createdat:asc")) {
                return cb.asc(root.get("createdAt"));
            }
        }
        return cb.asc(root.get("createdAt"));
    }
}
