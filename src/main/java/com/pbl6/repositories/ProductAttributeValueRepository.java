package com.pbl6.repositories;

import com.pbl6.entities.ProductAttributeValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValueEntity, Long> {

    @Query("""
        select pav
        from ProductAttributeValueEntity pav
        join fetch pav.attribute a
        join fetch pav.attributeValue v
        where pav.product.id = :productId
          and a.isOption = true
    """)
    List<ProductAttributeValueEntity> findOptionAttributesByProductId(@Param("productId") Long productId);

    @Query("SELECT pav FROM ProductAttributeValueEntity pav " +
            "JOIN FETCH pav.attribute a " +
            "JOIN FETCH pav.attributeValue av " +
            "WHERE pav.product.id IN :productIds " +
            "AND a.isOption = true " +
            "ORDER BY a.code, av.value")
    List<ProductAttributeValueEntity> findOptionAttributesByProductIds(@Param("productIds") List<Long> productIds);
}

