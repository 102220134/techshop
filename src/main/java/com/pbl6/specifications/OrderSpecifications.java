package com.pbl6.specifications;

import com.pbl6.entities.OrderEntity;
import com.pbl6.entities.UserEntity;
import com.pbl6.enums.ReceiveMethod;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderSpecifications {

    public static Specification<OrderEntity> byPermissions(UserEntity user) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (user.hasAuthority("ORDER_READ_ONLINE")) {
                predicates.add(cb.isTrue(root.get("isOnline")));
            }

            if (user.hasAuthority("ORDER_READ_OFFLINE")) {
                if (user.getStoreId() == null) {
                    return cb.disjunction();
                }

                Predicate offlineStore = cb.and(
                        cb.isFalse(root.get("isOnline")),
                        cb.equal(root.get("store").get("id"), user.getStoreId())
                );

                Predicate pickupOnline = cb.and(
                        cb.isTrue(root.get("isOnline")),
                        cb.equal(root.get("receiveMethod"), ReceiveMethod.RECEIVE_AT_STORE),
                        cb.equal(root.get("store").get("id"), user.getStoreId())
                );

                predicates.add(cb.or(offlineStore, pickupOnline));
            }

            return predicates.isEmpty()
                    ? cb.disjunction()
                    : cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<OrderEntity> byStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<OrderEntity> byKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String likePattern = "%" + keyword.trim().toLowerCase() + "%";

            List<Predicate> fields = new ArrayList<>();
            fields.add(cb.like(cb.lower(root.get("snapshotName")), likePattern));
            fields.add(cb.like(cb.lower(root.get("snapshotPhone")), likePattern));

            return cb.or(fields.toArray(new Predicate[0]));
        };
    }
    public static Specification<OrderEntity> byDateRange(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();

            if (start != null) {
                preds.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }
            if (end != null) {
                preds.add(cb.lessThanOrEqualTo(root.get("createdAt"), end));
            }

            return preds.isEmpty() ? cb.conjunction() : cb.and(preds.toArray(new Predicate[0]));
        };
    }
}
