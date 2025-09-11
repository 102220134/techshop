package com.pbl6.repositories;

import com.pbl6.entities.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    List<RefreshTokenEntity> findByUserId(long userId);
    Optional<RefreshTokenEntity> findByToken(String refreshToken);
}
