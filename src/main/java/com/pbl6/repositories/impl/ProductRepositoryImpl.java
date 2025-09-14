package com.pbl6.repositories.impl;

import com.pbl6.dtos.request.product.ProductFilterRequest;
import com.pbl6.dtos.response.ProductDto;
import com.pbl6.repositories.ProductRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<ProductDto> searchProducts(Long categoryId, ProductFilterRequest req, boolean includeInactive, boolean isOnlyInStock) {
        StringBuilder sql = new StringBuilder();
        sql.append("""
                    SELECT p.id, p.name, p.description, p.slug, p.thumbnail,
                           MIN(CASE WHEN v.is_active = 1 THEN v.price END) AS price,
                           COALESCE(SUM(CASE WHEN v.is_active = 1 THEN i.stock ELSE 0 END), 0) AS stock,
                           COALESCE(SUM(CASE WHEN v.is_active = 1 THEN i.reserved_stock ELSE 0 END), 0) AS reservedStock,
                           COALESCE(SUM(oi.quantity),0) AS sold,
                           COUNT(r.id) AS totalReview,
                           AVG(r.rating) AS avgRating
                    FROM products p
                    LEFT JOIN variants v ON v.product_id = p.id
                    LEFT JOIN inventories i ON i.variant_id = v.id
                    LEFT JOIN order_items oi ON oi.variant_id = v.id
                    LEFT JOIN reviews r ON r.product_id = p.id
                    JOIN category_products cp ON cp.product_id = p.id
                    WHERE 1=1
                """);


        // filter category
        if (categoryId != null) {
            sql.append(" AND cp.category_id = :categoryId ");
        }

        // filter active/inactive
        if (!includeInactive) {
            sql.append(" AND p.is_active = true ");
        }

        // filter giá
        if (req.getPrice_from() != null) sql.append(" AND v.price >= :priceFrom ");
        if (req.getPrice_to() != null) sql.append(" AND v.price <= :priceTo ");

        // filter attributes
        if (req.getFilter() != null && !req.getFilter().isEmpty()) {
            for (String key : req.getFilter().keySet()) {
                sql.append("""
                            AND EXISTS (
                               SELECT 1
                               FROM product_attribute_values pav
                               JOIN attributes a ON a.id = pav.attribute_id
                               JOIN attribute_values av ON av.id = pav.value_id
                               WHERE pav.product_id = p.id
                                 AND a.code = :attrCode_%s
                                 AND av.value IN (:attrVals_%s)
                            )
                        """.formatted(key, key));
            }
        }

        sql.append(" GROUP BY p.id ");

        if (isOnlyInStock) {
            sql.append(" HAVING (stock - reservedStock) > 0 ");
        }

        // sort
        Map<String, String> sortMap = Map.of(
                "price", "price",
                "stock", "stock",
                "sold", "sold",
                "rating", "avgRating",
                "createdAt", "p.created_at"
        );
        String orderCol = sortMap.getOrDefault(req.getOrder(), "p.id");
        String direction = "desc".equalsIgnoreCase(req.getDir()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderCol).append(" ").append(direction);

        Query query = em.createNativeQuery(sql.toString());

        // gán params
        if (categoryId != null) query.setParameter("categoryId", categoryId);
        if (req.getPrice_from() != null) query.setParameter("priceFrom", req.getPrice_from());
        if (req.getPrice_to() != null) query.setParameter("priceTo", req.getPrice_to());
        if (req.getFilter() != null) {
            for (Map.Entry<String, List<String>> entry : req.getFilter().entrySet()) {
                query.setParameter("attrCode_" + entry.getKey(), entry.getKey());
                query.setParameter("attrVals_" + entry.getKey(), entry.getValue());
            }
        }

        // phân trang
        int offset = (req.getPage() - 1) * req.getSize();
        query.setFirstResult(offset);
        query.setMaxResults(req.getSize());

        // map thủ công
        List<Object[]> rows = query.getResultList();
        List<ProductDto> result = rows.stream()
                .map(r -> ProductDto.builder()
                        .id(((Number) r[0]).longValue())
                        .name((String) r[1])
                        .description((String) r[2])
                        .slug((String) r[3])
                        .thumbnail((String) r[4])
                        .price((BigDecimal) r[5])
                        .stock(((Number) r[6]).intValue())
                        .reservedStock(((Number) r[7]).intValue())
                        .availableStock((((Number) r[6]).intValue()) - (((Number) r[7]).intValue()))
                        .sold(((Number) r[8]).intValue())
                        .rating(new ProductDto.RatingSummary(
                                ((Number) r[9]).longValue(),
                                r[10] != null ? ((Number) r[10]).doubleValue() : 0.0
                        ))

                        .build()
                )
                .toList();

        // count query
        StringBuilder countSql = new StringBuilder();
        countSql.append("""
                    SELECT COUNT(DISTINCT p.id)
                    FROM products p
                    JOIN category_products cp ON cp.product_id = p.id
                    WHERE 1=1
                """);
        if (categoryId != null) countSql.append(" AND cp.category_id = :categoryId ");
        if (!includeInactive) {
            countSql.append(" AND p.is_active = true ");
        }
        if (req.getPrice_from() != null)
            countSql.append(" AND EXISTS (SELECT 1 FROM variants v WHERE v.product_id=p.id AND v.price >= :priceFrom) ");
        if (req.getPrice_to() != null)
            countSql.append(" AND EXISTS (SELECT 1 FROM variants v WHERE v.product_id=p.id AND v.price <= :priceTo) ");
        if (req.getFilter() != null && !req.getFilter().isEmpty()) {
            for (String key : req.getFilter().keySet()) {
                countSql.append("""
                            AND EXISTS (
                               SELECT 1
                               FROM product_attribute_values pav
                               JOIN attributes a ON a.id = pav.attribute_id
                               JOIN attribute_values av ON av.id = pav.value_id
                               WHERE pav.product_id = p.id
                                 AND a.code = :attrCode_%s
                                 AND av.value IN (:attrVals_%s)
                            )
                        """.formatted(key, key));
            }
        }

        if (isOnlyInStock) {
            countSql.append("""
                        AND EXISTS (
                            SELECT 1
                            FROM variants v
                            LEFT JOIN inventories i ON i.variant_id = v.id
                            WHERE v.product_id = p.id
                              AND v.is_active = 1
                            GROUP BY v.product_id
                            HAVING (COALESCE(SUM(i.stock),0) - COALESCE(SUM(i.reserved_stock),0)) > 0
                        )
                    """);
        }


        Query countQuery = em.createNativeQuery(countSql.toString());
        if (categoryId != null) countQuery.setParameter("categoryId", categoryId);
        if (req.getPrice_from() != null) countQuery.setParameter("priceFrom", req.getPrice_from());
        if (req.getPrice_to() != null) countQuery.setParameter("priceTo", req.getPrice_to());
        if (req.getFilter() != null) {
            for (Map.Entry<String, List<String>> entry : req.getFilter().entrySet()) {
                countQuery.setParameter("attrCode_" + entry.getKey(), entry.getKey());
                countQuery.setParameter("attrVals_" + entry.getKey(), entry.getValue());
            }
        }

        long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(result, PageRequest.of(req.getPage() - 1, req.getSize()), total);
    }
}

