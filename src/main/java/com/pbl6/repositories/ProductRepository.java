package com.pbl6.repositories;

import com.pbl6.entities.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {

    Optional<ProductEntity> findBySlug(String slug);

    @Query(value = """
                SELECT DISTINCT p.*
                FROM products p
                WHERE p.id IN (
                    SELECT pr.related_product_id
                    FROM product_relations pr
                    WHERE pr.product_id = :productId
                    UNION
                    SELECT pr.product_id
                    FROM product_relations pr
                    WHERE pr.related_product_id = :productId
                )
            """, nativeQuery = true)
    List<ProductEntity> findSiblingsByProductId(@Param("productId") Long productId);

    @EntityGraph(attributePaths = {"variants", "medias", "relatedProducts"})
    Optional<ProductEntity> findBySlugAndIsActive(String slug, boolean active);

    @Query("""
                SELECT p FROM UserEntity u
                JOIN u.likedProducts p
                WHERE u.id = :userId
            """)
    Page<ProductEntity> findLikedProductsByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );


}
