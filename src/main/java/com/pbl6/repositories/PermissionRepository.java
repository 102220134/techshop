package com.pbl6.repositories;

import com.pbl6.entities.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, Long>
{
    Set<PermissionEntity> findByNameIn(List<String> names);
}
