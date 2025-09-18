package com.pbl6.constants;

/**
 * Constants for database queries to improve maintainability and performance
 */
public final class QueryConstants {

    private QueryConstants() {
        // Utility class
    }

    // Simplified product search query using subqueries for better performance
    public static final String PRODUCT_SEARCH_BASE = """
            SELECT
                p.id,
                p.name,
                p.description,
                p.slug,
                p.thumbnail,
                p.detail,
                p.created_at AS createdAt,
                p.updated_at AS updatedAt,
                (SELECT MIN(v.price) FROM variants v WHERE v.product_id = p.id AND v.is_active = 1) AS price,
                (SELECT COALESCE(SUM(i.stock), 0) FROM variants v
                 LEFT JOIN inventories i ON i.variant_id = v.id
                 WHERE v.product_id = p.id AND v.is_active = 1) AS stock,
                (SELECT COALESCE(SUM(i.reserved_stock), 0) FROM variants v
                 LEFT JOIN inventories i ON i.variant_id = v.id
                 WHERE v.product_id = p.id AND v.is_active = 1) AS reservedStock,
                (SELECT COALESCE(SUM(oi.quantity), 0) FROM variants v
                 LEFT JOIN order_items oi ON oi.variant_id = v.id
                 WHERE v.product_id = p.id AND v.is_active = 1) AS sold,
                (SELECT COUNT(*) FROM reviews r WHERE r.product_id = p.id) AS total,
                (SELECT COALESCE(AVG(r.rating), 0) FROM reviews r WHERE r.product_id = p.id) AS average
            FROM products p
            INNER JOIN category_products cp ON cp.product_id = p.id
            WHERE 1=1
            """;

    public static final String PRODUCT_COUNT_BASE = """
            SELECT COUNT(DISTINCT p.id)
            FROM products p
            INNER JOIN category_products cp ON cp.product_id = p.id
            WHERE 1=1
            """;

    // Attribute filter subquery - optimized with proper indexes
    public static final String ATTRIBUTE_FILTER_EXISTS = """
            AND EXISTS (
                SELECT 1
                FROM product_attribute_values pav
                INNER JOIN attributes a ON a.id = pav.attribute_id
                INNER JOIN attribute_values av ON av.id = pav.value_id
                WHERE pav.product_id = p.id
                  AND a.code = :attrCode_%s
                  AND av.value IN (:attrVals_%s)
            )
            """;

    // Stock availability check - optimized
    public static final String STOCK_AVAILABILITY_EXISTS = """
            AND EXISTS (
                SELECT 1
                FROM variants v
                INNER JOIN inventories i ON i.variant_id = v.id
                WHERE v.product_id = p.id
                  AND v.is_active = 1
                GROUP BY v.product_id
                HAVING (COALESCE(SUM(i.stock), 0) - COALESCE(SUM(i.reserved_stock), 0)) > 0
            )
            """;

    // Price range filters for count query
    public static final String PRICE_FROM_EXISTS = """
            AND EXISTS (
                SELECT 1 FROM variants v 
                WHERE v.product_id = p.id 
                  AND v.is_active = 1 
                  AND v.price >= :priceFrom
            )
            """;

    public static final String PRICE_TO_EXISTS = """
            AND EXISTS (
                SELECT 1 FROM variants v 
                WHERE v.product_id = p.id 
                  AND v.is_active = 1 
                  AND v.price <= :priceTo
            )
            """;

    // Sorting options
    public static final String[] VALID_SORT_FIELDS = {
            "id", "price", "stock", "sold", "rating", "createdAt"
    };

    // Pagination limits
    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_NUMBER = 1000;
}