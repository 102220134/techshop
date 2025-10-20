package com.pbl6.constants;

/**
 * Constants for database queries to improve maintainability and performance.
 * Now uses the variant_effective_price view to fetch final prices (after promotions).
 */
public final class QueryConstants {

    private QueryConstants() {
        // Utility class
    }

    /**
     * Product search base query with joins to:
     * - variant_effective_price (for final discounted price)
     * - inventories (for stock)
     * - order_items (for sold)
     * - reviews (for rating)
     */
    public static final String PRODUCT_SEARCH_BASE = """
                SELECT DISTINCT
                    p.id,
                    p.name,
                    p.description,
                    p.slug,
                    p.thumbnail,
                    p.detail,
                    p.created_at AS createdAt,
                    p.updated_at AS updatedAt,
            
                    -- ✅ Giá gốc
                    (SELECT MIN(v.price) FROM variants v WHERE v.product_id = p.id AND v.is_active = 1) AS price,
            
                    -- ✅ Giá sau khuyến mãi
                    (SELECT MIN(vep.effective_price)
                    FROM variant_effective_price vep
                    INNER JOIN variants v ON v.id = vep.variant_id
                    WHERE vep.product_id = p.id AND v.is_active = 1) AS discounted_price,
            
            
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


    /**
     * Count base query (uses DISTINCT product_id to avoid duplication)
     */
    public static final String PRODUCT_COUNT_BASE = """
            SELECT COUNT(DISTINCT p.id)
            FROM products p
            INNER JOIN category_products cp ON cp.product_id = p.id
            LEFT JOIN variant_effective_price vep ON vep.product_id = p.id
            WHERE 1=1
            """;

    /**
     * Attribute filter clause (same as before).
     */
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

    /**
     * Stock availability check (same as before).
     */
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

    /**
     * ✅ Price filters now use variant_effective_price (after promotion)
     */
    public static final String PRICE_FROM_EXISTS = """
            AND EXISTS (
                SELECT 1 FROM variant_effective_price vep
                WHERE vep.product_id = p.id
                  AND vep.effective_price >= :priceFrom
            )
            """;

    public static final String PRICE_TO_EXISTS = """
            AND EXISTS (
                SELECT 1 FROM variant_effective_price vep
                WHERE vep.product_id = p.id
                  AND vep.effective_price <= :priceTo
            )
            """;

    /**
     * Sorting options — keep same fields, but "price" now maps to discounted price.
     */
    public static final String[] VALID_SORT_FIELDS = {"id", "price", "stock", "sold", "rating", "createdAt"};

    // Pagination limits
    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_NUMBER = 1000;
}
