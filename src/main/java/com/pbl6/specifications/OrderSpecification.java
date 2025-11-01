package com.pbl6.specifications;

import com.pbl6.dtos.request.order.SearchOrderRequest;
import com.pbl6.dtos.request.order.SearchOrderRequest;
import com.pbl6.entities.OrderEntity;
import com.pbl6.entities.UserEntity;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;

public class OrderSpecification {

    public static Specification<OrderEntity> hasIdEqual(Long id) {
        return (root, query, cb) ->
                id == null ? null : cb.equal(root.get("id"), id);
    }


    public static Specification<OrderEntity> hasStatus(Enum<?> status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<OrderEntity> isOnline(Boolean isOnline) {
        return (root, query, cb) ->
                isOnline == null ? null : cb.equal(root.get("isOnline"), isOnline);
    }

    public static Specification<OrderEntity> belongsToStore(Long storeId) {
        return (root, query, cb) ->
                storeId == null ? null : cb.equal(root.get("store").get("id"), storeId);
    }

    public static Specification<OrderEntity> hasReceiveMethod(Enum<?> method) {
        return (root, query, cb) ->
                method == null ? null : cb.equal(root.get("receiveMethod"), method);
    }

    public static Specification<OrderEntity> hasPaymentMethod(Enum<?> method) {
        return (root, query, cb) ->
                method == null ? null : cb.equal(root.get("paymentMethod"), method);
    }

    public static Specification<OrderEntity> hasCustomerKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            Join<OrderEntity, UserEntity> customer = root.join("user");
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(customer.get("name")), pattern),
                    cb.like(cb.lower(customer.get("phone")), pattern)
            );
        };
    }

    public static Specification<OrderEntity> createdAfter(java.time.LocalDateTime fromDate) {
        return (root, query, cb) ->
                fromDate == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
    }

    public static Specification<OrderEntity> createdBefore(java.time.LocalDateTime toDate) {
        return (root, query, cb) ->
                toDate == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), toDate);
    }

    /** Gộp tất cả lại — chỉ là optional tiện dụng */
    public static Specification<OrderEntity> build(SearchOrderRequest req) {
        return Specification
                .where(hasIdEqual(req.getId()))
                .and(hasStatus(req.getStatus()))
                .and(isOnline(req.getIsOnline()))
                .and(belongsToStore(req.getStoreId()))
                .and(hasReceiveMethod(req.getReceiveMethod()))
                .and(hasPaymentMethod(req.getPaymentMethod()))
                .and(hasCustomerKeyword(req.getCustomerKeyword()))
                .and(createdAfter(req.getFromDate()))
                .and(createdBefore(req.getToDate()));
    }
}
