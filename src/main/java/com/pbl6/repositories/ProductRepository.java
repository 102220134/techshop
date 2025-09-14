package com.pbl6.repositories;

import com.pbl6.dtos.projection.ProductProjection;
import com.pbl6.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity,Long> , JpaSpecificationExecutor<ProductEntity> {

    @Query(value = """
    SELECT t.*
    FROM (
        SELECT 
            p.id AS id,
            p.name AS name,
            p.description AS description,
            p.slug AS slug,
            p.thumbnail AS thumbnail,
            MIN(CASE WHEN v.is_active = 1 THEN v.price END) AS price,
            COALESCE(SUM(CASE WHEN v.is_active = 1 THEN inv.stock ELSE 0 END), 0) AS stock,
            COALESCE(SUM(CASE WHEN v.is_active = 1 THEN inv.reserved_stock ELSE 0 END), 0) AS reservedStock,
            COALESCE(SUM(oi.quantity), 0) AS sold,
            COUNT(r.id) AS total,
            COALESCE(AVG(r.rating), 0) AS average
        FROM products p
        LEFT JOIN variants v ON v.product_id = p.id
        LEFT JOIN inventories inv ON inv.variant_id = v.id
        LEFT JOIN order_items oi ON oi.variant_id = v.id
        LEFT JOIN reviews r ON r.product_id = p.id
        JOIN category_products cp ON cp.product_id = p.id
        JOIN categories c ON c.id = cp.category_id
        WHERE c.id = :cateId
          AND (:includeInactive = true OR p.is_active = true)
        GROUP BY p.id
    ) t
    """,
            nativeQuery = true)
    List<ProductProjection> findAllByCategoryId(
            @Param("cateId") Long cateId,
            @Param("includeInactive") boolean includeInactive
    );

    Optional<ProductEntity> findBySlug(String slug);

}
