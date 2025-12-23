package com.pbl6.repositories;

import com.pbl6.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);
    boolean existsByName(String name);
    Set<RoleEntity> findAllByNameIn(Collection<String> names);

}
