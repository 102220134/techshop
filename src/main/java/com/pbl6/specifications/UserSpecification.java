package com.pbl6.specifications;

import com.pbl6.entities.RoleEntity;
import com.pbl6.entities.UserEntity;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<UserEntity> hasName(String name) {
        return (root, query, cb) ->
                (name == null || name.isBlank())
                        ? null
                        : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<UserEntity> hasPhone(String phone) {
        return (root, query, cb) ->
                (phone == null || phone.isBlank())
                        ? null
                        : cb.like(cb.lower(root.get("phone")), phone + "%");
    }

    public static Specification<UserEntity> hasEmail(String email) {
        return (root, query, cb) ->
                (email == null || email.isBlank())
                        ? null
                        : cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
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

