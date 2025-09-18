package com.pbl6.repositories;

import com.pbl6.entities.ProductAttributeValueEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValueEntity, Long> {

    @EntityGraph(attributePaths = {"attribute", "attributeValue"})
    List<ProductAttributeValueEntity> findByProductIdIn(List<Long> product_id);
}

