package com.pbl6.specifications;

import com.pbl6.entities.RoleEntity;
import com.pbl6.entities.UserEntity;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<UserEntity> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }

            String lower = keyword.toLowerCase();
            String contains = "%" + lower + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("name")), contains),
                    cb.like(cb.lower(root.get("email")), contains),
                    cb.like(cb.lower(root.get("phone")), lower + "%")
            );
        };
    }


    public static Specification<UserEntity> isActive(Boolean active) {
        return (root, query, cb) ->
                active == null ? null : cb.equal(root.get("isActive"), active);
    }

    public static Specification<UserEntity> hasRoleLike(String pattern) {
        return (root, query, cb) -> {
            Join<UserEntity, RoleEntity> roleJoin = root.join("roles", JoinType.INNER);
            return cb.like(roleJoin.get("name"), pattern);
        };
    }

    public static Specification<UserEntity> hasRole(String roleName) {
        return (root, query, cb) -> {
            Join<UserEntity, RoleEntity> roleJoin = root.join("roles", JoinType.INNER);
            return cb.equal(roleJoin.get("name"), roleName);
        };
    }

    public static Specification<UserEntity> hasCustomerRole() {
        return hasRole("CUSTOMER");
    }

    public static Specification<UserEntity> hasStaffRole() {
        return hasRoleLike("STAFF_%");
    }
}

