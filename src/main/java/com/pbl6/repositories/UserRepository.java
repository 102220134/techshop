package com.pbl6.repositories;


import com.pbl6.entities.UserEntity;
import com.pbl6.utils.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByPhoneAndIsActive(String phone , boolean isActive);
    Optional<UserEntity> findByPhone(String phone);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByEmailAndIsActive(String email,boolean isActive);

    //dashboard
    @Query("SELECT COUNT(DISTINCT u) FROM UserEntity u " +
           "JOIN u.roles r " +
           "WHERE u.createdAt BETWEEN :start AND :end " +
           "AND r.name = 'CUSTOMER'")
    Long countNewCustomers(LocalDateTime start, LocalDateTime end);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);
}
