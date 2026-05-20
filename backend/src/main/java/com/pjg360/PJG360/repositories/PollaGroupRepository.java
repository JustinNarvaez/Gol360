package com.pjg360.PJG360.repositories;

import com.pjg360.PJG360.model.entities.PollaGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PollaGroupRepository extends JpaRepository<PollaGroup, Long> {

    Optional<PollaGroup> findByPollaCode(String pollaCode);
    boolean existsByPollaCode(String pollaCode);
}