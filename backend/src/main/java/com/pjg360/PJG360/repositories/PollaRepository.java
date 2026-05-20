package com.pjg360.PJG360.repositories;

import com.pjg360.PJG360.enums.PollaStatus;
import com.pjg360.PJG360.model.entities.Polla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollaRepository extends JpaRepository<Polla, Long> {

    List<Polla> findByStatus(PollaStatus status);
    List<Polla> findByOwnerId(Long ownerId);
}