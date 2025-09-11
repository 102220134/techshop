package com.pbl6.repositories;

import com.pbl6.dtos.projection.ProductProjection;
import com.pbl6.entities.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
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
        SELECT 
            p.id AS id,
            p.name AS name,
            p.description AS description,
            p.slug AS slug,
            p.detail AS detail,
            p.thumbnail AS thumbnail,
            MIN(v.price) AS price,
            COALESCE(SUM(inv.stock),0) AS stock,
            COALESCE(SUM(oi.quantity),0) AS sold,
            COUNT(r.id) AS total,
            SUM(CASE WHEN r.rating = 1 THEN 1 ELSE 0 END) AS star1,
            SUM(CASE WHEN r.rating = 2 THEN 1 ELSE 0 END) AS star2,
            SUM(CASE WHEN r.rating = 3 THEN 1 ELSE 0 END) AS star3,
            SUM(CASE WHEN r.rating = 4 THEN 1 ELSE 0 END) AS star4,
            SUM(CASE WHEN r.rating = 5 THEN 1 ELSE 0 END) AS star5,
            AVG(r.rating) AS average
        FROM products p
        LEFT JOIN variants v ON v.product_id = p.id
        LEFT JOIN inventories inv ON inv.variant_id = v.id
        LEFT JOIN order_items oi ON oi.variant_id = v.id
        LEFT JOIN reviews r ON r.product_id = p.id
        JOIN category_products cp ON cp.product_id = p.id
        JOIN categories c ON c.id = cp.category_id
        WHERE c.slug = :slug
          AND (:includeInactive = true OR p.is_active = true)
        GROUP BY p.id
        """,
            countQuery = """
        SELECT COUNT(DISTINCT p.id)
        FROM products p
        JOIN category_products cp ON cp.product_id = p.id
        JOIN categories c ON c.id = cp.category_id
        WHERE c.slug = :slug
          AND (:includeInactive = true OR p.is_active = true)
        """,
            nativeQuery = true)
    Page<ProductProjection> findProductsWithSlug(
            @Param("slug") String slug,
            @Param("includeInactive") boolean includeInactive,
            Pageable pageable
    );
}
