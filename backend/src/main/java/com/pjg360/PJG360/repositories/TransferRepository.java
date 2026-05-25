package com.pjg360.PJG360.repositories;

import com.pjg360.PJG360.model.entities.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findByOwnerId(Long ownerId);
    List<Transfer> findByNewOwnerId(Long newOwnerId);
}