package com.pbl6.repositories;


import com.pbl6.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByPhoneAndIsActiveTrue(String phone);
    Optional<UserEntity> findByPhone(String phone);
}
