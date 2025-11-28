package com.pbl6.repositories;

import com.pbl6.entities.AttributeValueEntity;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeValueRepository extends JpaRepository<AttributeValueEntity,Long> {
    Optional<AttributeValueEntity> findByValueAndAttributeId(String value, Long attrId);
    boolean existsByValueAndAttributeId(String value, Long id);
//    List<AttributeValueEntity> findAllInValuesAndAttributeId(List<String> values, Long attrId);
}
