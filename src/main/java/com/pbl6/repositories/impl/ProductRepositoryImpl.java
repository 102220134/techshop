package com.pbl6.repositories.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pbl6.constants.QueryConstants;
import com.pbl6.dtos.projection.ProductProjection;
import com.pbl6.dtos.request.product.ProductFilterRequest;
import com.pbl6.dtos.response.MediaDto;
import com.pbl6.dtos.response.ProductDetailDto;
import com.pbl6.dtos.response.VariantDto;
import com.pbl6.repositories.ProductRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final EntityManager em;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, String> SORT_MAPPING = Map.of(
            "price", "price",
            "stock", "stock",
            "sold", "sold",
            "rating", "average",
            "createdAt", "createdAt"
    );

    @Override
    public Page<ProductProjection> searchProducts(Long categoryId, ProductFilterRequest req, boolean includeInactive, boolean isOnlyInStock) {
        // Build main query using simplified approach
        StringBuilder mainSql = new StringBuilder(QueryConstants.PRODUCT_SEARCH_BASE);
        applyActive(mainSql, includeInactive);
        applyFilters(mainSql, categoryId, req);
        
        if (isOnlyInStock) {
            mainSql.append(" AND (SELECT COALESCE(SUM(i.stock), 0) - COALESCE(SUM(i.reserved_stock), 0) ")
                   .append("FROM variants v LEFT JOIN inventories i ON i.variant_id = v.id ")
                   .append("WHERE v.product_id = p.id AND v.is_active = 1) > 0 ");
        }
        
        applySorting(mainSql, req);
        
        // Execute main query
        Query mainQuery = em.createNativeQuery(mainSql.toString());
        setQueryParameters(mainQuery, categoryId, req);
        applyPagination(mainQuery, req);
        
        List<Object[]> rows = mainQuery.getResultList();
        List<ProductProjection> result = mapToProductProjection(rows);
        
        // Execute count query
        long total = executeCountQuery(categoryId, req, includeInactive, isOnlyInStock);
        
        return new PageImpl<>(result, PageRequest.of(req.getPage() - 1, req.getSize()), total);
    }

    @Override
    public List<ProductProjection> findAllByCategoryId(Long categoryId, boolean includeInactive, boolean isOnlyInStock) {
        StringBuilder mainSql = new StringBuilder(QueryConstants.PRODUCT_SEARCH_BASE);

        applyActive(mainSql, includeInactive);

        if (categoryId != null) {
            mainSql.append(" AND cp.category_id = :categoryId ");
        }

        if (isOnlyInStock) {
            mainSql.append(" AND (SELECT COALESCE(SUM(i.stock), 0) - COALESCE(SUM(i.reserved_stock), 0) ")
                    .append("FROM variants v LEFT JOIN inventories i ON i.variant_id = v.id ")
                    .append("WHERE v.product_id = p.id AND v.is_active = 1) > 0 ");
        }
        Query mainQuery = em.createNativeQuery(mainSql.toString());

        if (categoryId != null) {
            mainQuery.setParameter("categoryId", categoryId);
        }

        List<Object[]> rows = mainQuery.getResultList();

        return mapToProductProjection(rows);
    }

    @Override
    public Optional<ProductProjection> findBySlug(String slug, boolean includeInactive) {
        StringBuilder mainSql = new StringBuilder(QueryConstants.PRODUCT_SEARCH_BASE);

        applyActive(mainSql, includeInactive);

        if (slug != null) {
            mainSql.append(" AND p.slug = :slug ");
        }

        Query mainQuery = em.createNativeQuery(mainSql.toString());

        if (slug != null) {
            mainQuery.setParameter("slug", slug);
        }

        List<Object[]> rows = mainQuery.getResultList();

        return rows.stream()
                .findFirst()
                .map(this::mapRowToProjection);
    }

    private void applyActive(StringBuilder sql, boolean includeInactive) {
        if (!includeInactive) {
            sql.append(" AND p.is_active = true ");
        }
    }

    private void applyFilters(StringBuilder sql, Long categoryId, ProductFilterRequest req) {
        // Category filter
        if (categoryId != null) {
            sql.append(" AND cp.category_id = :categoryId ");
        }
//
//        // Active/inactive filter
//        if (!includeInactive) {
//            sql.append(" AND p.is_active = true ");
//        }

        // Price filters - simplified since we use subqueries
        if (req.getPrice_from() != null) {
            sql.append(" AND (SELECT MIN(v.price) FROM variants v WHERE v.product_id = p.id AND v.is_active = 1) >= :priceFrom ");
        }
        
        if (req.getPrice_to() != null) {
            sql.append(" AND (SELECT MIN(v.price) FROM variants v WHERE v.product_id = p.id AND v.is_active = 1) <= :priceTo ");
        }

        // Attribute filters
        if (req.getFilter() != null && !req.getFilter().isEmpty()) {
            for (String key : req.getFilter().keySet()) {
                sql.append(QueryConstants.ATTRIBUTE_FILTER_EXISTS.formatted(key, key));
            }
        }
    }
    
    private void applySorting(StringBuilder sql, ProductFilterRequest req) {
        String orderCol = SORT_MAPPING.getOrDefault(req.getOrder(), "p.id");
        String direction = "desc".equalsIgnoreCase(req.getDir()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderCol).append(" ").append(direction);
    }
    
    private void setQueryParameters(Query query, Long categoryId, ProductFilterRequest req) {
        if (categoryId != null) {
            query.setParameter("categoryId", categoryId);
        }
        
        if (req.getPrice_from() != null) {
            query.setParameter("priceFrom", req.getPrice_from());
        }
        
        if (req.getPrice_to() != null) {
            query.setParameter("priceTo", req.getPrice_to());
        }
        
        if (req.getFilter() != null) {
            for (Map.Entry<String, List<String>> entry : req.getFilter().entrySet()) {
                query.setParameter("attrCode_" + entry.getKey(), entry.getKey());
                query.setParameter("attrVals_" + entry.getKey(), entry.getValue());
            }
        }
    }
    
    private void applyPagination(Query query, ProductFilterRequest req) {
        int offset = (req.getPage() - 1) * req.getSize();
        query.setFirstResult(offset);
        query.setMaxResults(req.getSize());
    }
    
    private List<ProductProjection> mapToProductProjection(List<Object[]> rows) {
        return rows.stream()
                .map(this::mapRowToProjection)
                .toList();
    }
    
    private ProductProjection mapRowToProjection(Object[] row) {
        return new ProductProjection() {
            @Override
            public Long getId() {
                return ((Number) row[0]).longValue();
            }

            @Override
            public String getName() {
                return (String) row[1];
            }

            @Override
            public String getDescription() {
                return (String) row[2];
            }

            @Override
            public String getSlug() {
                return (String) row[3];
            }

            @Override
            public String getThumbnail() {
                return (String) row[4];
            }

            @Override
            public ObjectNode getDetail() {
                try {
                    String detailJson = (String) row[5]; // DB trả về raw JSON dạng text
                    if (detailJson == null || detailJson.isBlank()) {
                        return objectMapper.createObjectNode(); // fallback: object rỗng
                    }
                    // Parse thành ObjectNode
                    return (ObjectNode) objectMapper.readTree(detailJson);
                } catch (Exception e) {
                    System.err.println("❌ JSON parse error in detail column: " + e.getMessage());
                    return objectMapper.createObjectNode();
                }
            }


            @Override
            public LocalDateTime getCreatedAt() {
                return (LocalDateTime) row[6];
            }

            @Override
            public LocalDateTime getUpdatedAt() {
                return (LocalDateTime) row[7];
            }

            @Override
            public BigDecimal getPrice() {
                return (BigDecimal) row[8];
            }

            @Override
            public Integer getStock() {
                return row[9] != null ? ((Number) row[9]).intValue() : 0;
            }

            @Override
            public Integer getReservedStock() {
                return row[10] != null ? ((Number) row[10]).intValue() : 0;
            }

            @Override
            public Integer getSold() {
                return row[11] != null ? ((Number) row[11]).intValue() : 0;
            }

            @Override
            public Long getTotal() {
                return row[12] != null ? ((Number) row[12]).longValue() : 0L;
            }

            @Override
            public Double getAverage() {
                return row[13] != null ? ((Number) row[13]).doubleValue() : 0.0;
            }
        };
    }
    
    private long executeCountQuery(Long categoryId, ProductFilterRequest req, 
                                  boolean includeInactive, boolean isOnlyInStock) {
        StringBuilder countSql = new StringBuilder(QueryConstants.PRODUCT_COUNT_BASE);
        applyActive(countSql, includeInactive);
        applyFilters(countSql, categoryId, req);
        
        if (isOnlyInStock) {
            countSql.append(QueryConstants.STOCK_AVAILABILITY_EXISTS);
        }
        
        Query countQuery = em.createNativeQuery(countSql.toString());
        setQueryParameters(countQuery, categoryId, req);
        
        return ((Number) countQuery.getSingleResult()).longValue();
    }

}
