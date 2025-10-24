package com.pbl6.repositories;

import com.pbl6.entities.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Optional<CategoryEntity> findBySlugAndParentIdAndIsActiveTrue(String slug, Long parentId);
    List<CategoryEntity> findByParentId(Long parentId);
    List<CategoryEntity> findByParentIdAndCategoryType(Long parentId, String categoryType);
}
