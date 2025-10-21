package com.pbl6.specifications;
import com.pbl6.entities.ProductAttributeValueEntity;
import com.pbl6.entities.ProductEntity;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ProductSpecifications {

    public static Specification<ProductEntity> isActive(boolean includeInactive) {
        return (root, query, cb) -> includeInactive ? cb.conjunction() : cb.isTrue(root.get("isActive"));
    }

    public static Specification<ProductEntity> byCategory(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return cb.conjunction();
            // Vì Product có ManyToMany Category, join trực tiếp
            Join<Object, Object> categoryJoin = root.join("categories", JoinType.INNER);
            return cb.equal(categoryJoin.get("id"), categoryId);
        };
    }

    public static Specification<ProductEntity> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("description")), like),
                    cb.like(cb.lower(root.get("slug")), like)
            );
        };
    }

    public static Specification<ProductEntity> priceRange(BigDecimal priceFrom, BigDecimal priceTo) {
        return (root, query, cb) -> {
            if (priceFrom != null && priceTo != null)
                return cb.between(root.get("discountedPrice"), priceFrom, priceTo);
            if (priceFrom != null)
                return cb.greaterThanOrEqualTo(root.get("discountedPrice"), priceFrom);
            if (priceTo != null)
                return cb.lessThanOrEqualTo(root.get("discountedPrice"), priceTo);
            return cb.conjunction();
        };
    }

    public static Specification<ProductEntity> onlyInStock(boolean onlyInStock) {
        return (root, query, cb) ->
                onlyInStock ? cb.greaterThan(root.get("stock"), 0) : cb.conjunction();
    }

    public static Specification<ProductEntity> attributes(Map<String, List<String>> filters) {
        return (root, query, cb) -> {
            if (filters == null || filters.isEmpty()) return cb.conjunction();

            Predicate combined = cb.conjunction();

            for (Map.Entry<String, List<String>> entry : filters.entrySet()) {
                String code = entry.getKey();
                List<String> values = entry.getValue();

                // Subquery tương đương với ATTRIBUTE_FILTER_EXISTS
                Subquery<Long> sub = query.subquery(Long.class);
                Root<ProductAttributeValueEntity> pav = sub.from(ProductAttributeValueEntity.class);
                Join<Object, Object> attr = pav.join("attribute");
                Join<Object, Object> val = pav.join("attributeValue");

                sub.select(pav.get("product").get("id"))
                        .where(
                                cb.equal(pav.get("product").get("id"), root.get("id")),
                                cb.equal(attr.get("code"), code),
                                val.get("value").in(values)
                        );

                combined = cb.and(combined, cb.exists(sub));
            }
            return combined;
        };
    }
}
