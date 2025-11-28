package com.pbl6.repositories;

import com.pbl6.entities.VariantAttributeValueEntity;
import com.pbl6.entities.VariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantAttributeValueRepository extends JpaRepository<VariantAttributeValueEntity,Long> {
    boolean existsByAttributeValueId(Long attributeValueId);

    List<VariantAttributeValueEntity> findByVariant(VariantEntity variant);
}
