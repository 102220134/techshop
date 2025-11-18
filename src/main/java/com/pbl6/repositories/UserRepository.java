package com.pbl6.repositories;


import com.pbl6.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByPhoneAndIsActive(String phone , boolean isActive);
    Optional<UserEntity> findByPhone(String phone);
    Optional<UserEntity> findByEmailAndIsActive(String email,boolean isActive);

    //dashboard
    @Query("SELECT COUNT(DISTINCT u) FROM UserEntity u " +
           "JOIN u.roles r " +
           "WHERE u.createdAt BETWEEN :start AND :end " +
           "AND r.name = 'CUSTOMER'")
    Long countNewCustomers(LocalDateTime start, LocalDateTime end);
}
