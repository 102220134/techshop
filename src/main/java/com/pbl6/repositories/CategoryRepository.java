package com.pbl6.repositories;

import com.pbl6.entities.CategoryEntity;
import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Optional<CategoryEntity> findBySlugAndParentIdAndIsActiveTrue(String slug, Long parentId);
    List<CategoryEntity> findByParentId(Long parentId);
    List<CategoryEntity> findByParentIdAndCategoryType(Long parentId, String categoryType);
}
