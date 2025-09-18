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

    Optional<ProductEntity> findBySlug(String slug);

    @Query(value = """
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
        WHERE cp.category_id = :cateId
          AND (:includeInactive = true OR p.is_active = true)
    """, nativeQuery = true)
    List<ProductProjection> findAllByCategoryId(
            @Param("cateId") Long cateId,
            @Param("includeInactive") boolean includeInactive
    );

}
