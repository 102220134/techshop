package com.pbl6.repositories;

import com.pbl6.entities.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAdressRepository extends JpaRepository<UserAddressEntity,Long> {
}
