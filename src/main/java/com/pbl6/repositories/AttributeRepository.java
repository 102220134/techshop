package com.pbl6.repositories;

import com.pbl6.entities.AttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeRepository extends JpaRepository<AttributeEntity,Long> {

    List<AttributeEntity> findAllByIsFilterTrue();

    List<AttributeEntity> findAllByIsOptionTrue();

    Optional<AttributeEntity> findByCodeAndIsFilterTrue(String code);


    Optional<AttributeEntity> findByCodeAndIsOptionTrue(String code);
}
