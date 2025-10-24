package com.pbl6.repositories;

import com.pbl6.entities.VariantAttributeValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VariantAttributeValueRepository extends JpaRepository<VariantAttributeValueEntity,Long> {
}
