package com.rdm.repository;

import com.rdm.model.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Integer> {
    Optional<UserGroup> findByName(String name);
    boolean existsByName(String name);
}

