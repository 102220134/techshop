package com.pbl6.repositories;

import com.pbl6.entities.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity,Long> {
    RoomEntity findByUserKey(String userKey);
}
