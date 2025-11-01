package com.pbl6.repositories;

import com.pbl6.entities.UserAddressEntity;
import com.pbl6.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddressEntity,Long> {
    List<UserAddressEntity> findByUserAndIsDefaultTrue(UserEntity user);
    long countByUserAndIsDefaultTrueAndIdNot(UserEntity user, Long id);
    long countByUserAndIsDefaultTrue(UserEntity user);
    long countByUser(UserEntity user);
    Optional<UserAddressEntity> findFirstByUserAndIdNot(UserEntity user,Long id);
}
