package com.pbl6.repositories;

import com.pbl6.entities.ProductAttributeValueEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValueEntity, Long> {

    @EntityGraph(attributePaths = {"attribute", "attributeValue"})
    List<ProductAttributeValueEntity> findByProductIdIn(List<Long> product_id);

    @EntityGraph(attributePaths = {"attribute", "attributeValue"})
    @Query("""
    SELECT pav
    FROM ProductAttributeValueEntity pav
    JOIN pav.attribute a
    WHERE pav.product.id = :productId
      AND a.isOption = true
""")
    List<ProductAttributeValueEntity> findOptionAttributesByProductId(@Param("productId") Long productId);

    @Query("SELECT DISTINCT pav FROM ProductAttributeValueEntity pav " +
           "JOIN FETCH pav.attribute attr " +
           "JOIN FETCH pav.attributeValue av " +
           "JOIN pav.product p " +
           "JOIN p.categories c " + // ✅ JOIN vào list categories
           "WHERE c.id = :categoryId AND p.isActive = true")
    List<ProductAttributeValueEntity> findAllByCategory(@Param("categoryId") Long categoryId);

    boolean existsByAttributeValueId(Long attributeValueId);

    List<ProductAttributeValueEntity> findByProductId(Long id);
}

