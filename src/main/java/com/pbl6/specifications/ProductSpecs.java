package com.pbl6.specifications;

import com.pbl6.entities.CategoryEntity;
import com.pbl6.entities.ProductAttributeValueEntity;
import com.pbl6.entities.ProductEntity;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class ProductSpecs {


    public static Specification<ProductEntity> hasCategorySlug(String slug) {
        return (root, query, cb) -> {
            Join<ProductEntity, CategoryEntity> categoryJoin = root.join("categories", JoinType.INNER);
            return cb.equal(categoryJoin.get("slug"), slug);
        };
    }

    public  Specification<ProductEntity> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }


    /** products thuộc ANY của categoryIds */
    public Specification<ProductEntity> inCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return null;
        return (root, cq, cb) -> {
            // join bảng trung gian category_products (ManyToMany)
            // mapping: Product.categories -> Category
            Join<Object, Object> cats = root.join("categories", JoinType.INNER);
            CriteriaBuilder.In<Long> in = cb.in(cats.get("id"));
            categoryIds.forEach(in::value);
            cq.distinct(true); // tránh duplicate do join
            return in;
        };
    }

    /**
     * AND theo từng attribute code, OR trong các giá trị của code đó.
     * Mỗi code build một EXISTS subquery lên (product_attribute_values ⋈ attributes ⋈ attribute_values).
     * filters: code -> [value1, value2, ...]
     */
    public static Specification<ProductEntity> hasAttributes(Map<String, List<String>> filters) {
        if (filters == null || filters.isEmpty()) return null;

        // lọc bỏ code rỗng / list rỗng
        Map<String, List<String>> cleaned = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> e : filters.entrySet()) {
            String code = e.getKey();
            List<String> vals = e.getValue();
            if (code == null || code.isBlank() || vals == null || vals.isEmpty()) continue;
            List<String> trimmed = vals.stream().filter(v -> v != null && !v.isBlank()).toList();
            if (!trimmed.isEmpty()) cleaned.put(code.trim(), trimmed);
        }
        if (cleaned.isEmpty()) return null;

        return (root, cq, cb) -> {
            List<Predicate> andPreds = new ArrayList<>();

            for (Map.Entry<String, List<String>> ent : cleaned.entrySet()) {
                String code = ent.getKey();
                List<String> values = ent.getValue();

                // EXISTS (
                //   SELECT 1
                //   FROM product_attribute_values pav
                //   JOIN attributes a ON a.id = pav.attribute_id
                //   JOIN attribute_values av ON av.id = pav.value_id
                //   WHERE pav.product_id = product.id
                //     AND a.code = :code
                //     AND av.value IN (:values)
                // )
                Subquery<Long> sq = cq.subquery(Long.class);
                Root<?> pav = sq.from(ProductAttributeValueEntity.class); // entity PAV
                Join<?, ?> a = pav.join("attribute");
                Join<?, ?> av = pav.join("attributeValue"); // value_id -> AttributeValue

                sq.select(cb.literal(1L))
                        .where(
                                cb.equal(pav.get("product").get("id"), root.get("id")),
                                cb.equal(a.get("code"), code),
                                av.get("value").in(values)
                        );

                andPreds.add(cb.exists(sq));
            }

            return cb.and(andPreds.toArray(new Predicate[0]));
        };
    }
    public  Specification<ProductEntity> priceGte(BigDecimal min) {
        if (min == null) return null;
        return (root, cq, cb) -> cb.ge(root.get("price"), min);
    }

    public  Specification<ProductEntity> priceLte(BigDecimal max) {
        if (max == null) return null;
        return (root, cq, cb) -> cb.le(root.get("price"), max);
    }

    public  Specification<ProductEntity> isParent() {
        return (root, cq, cb) -> cb.isTrue(root.get("isParent"));
    }
}

