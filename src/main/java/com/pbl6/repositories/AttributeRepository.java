package com.pbl6.repositories;

import com.pbl6.entities.AttributeEntity;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeRepository extends JpaRepository<AttributeEntity,Long> {

//    List<AttributeEntity> findAllByIsFilterTrue();
//
//    List<AttributeEntity> findAllByIsOptionTrue();

    // Query này dùng JOIN FETCH để lấy luôn Values, tránh lỗi N+1
    @Query("SELECT DISTINCT a FROM AttributeEntity a LEFT JOIN FETCH a.values WHERE a.isOption = true")
    List<AttributeEntity> findAllByIsOptionTrueWithValues();

    @Query("SELECT DISTINCT a FROM AttributeEntity a " +
           "LEFT JOIN FETCH a.values " +
           "WHERE a.isFilter = true AND a.category.id IN :categoryIds")
    List<AttributeEntity> findAllByCategoryIdInAndIsFilterTrue(@Param("categoryIds") List<Long> categoryIds);

    Optional<AttributeEntity> findByCodeAndIsFilterTrue(String code);


    Optional<AttributeEntity> findByCodeAndIsOptionTrue(String code);

    Optional<AttributeEntity> findByCode(String code);
}
