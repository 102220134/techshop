package com.pbl6.repositories;

import com.pbl6.entities.CartItemEntity;
import com.pbl6.entities.UserEntity;
import com.pbl6.entities.VariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    Optional<CartItemEntity> findByUserIdAndVariantId(Long userId, Long variantId);
    List<CartItemEntity> findAllByUserId(Long userId );
}
