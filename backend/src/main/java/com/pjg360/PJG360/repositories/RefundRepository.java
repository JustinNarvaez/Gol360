package com.pjg360.PJG360.repositories;

import com.pjg360.PJG360.model.entities.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByOwnerId(Long ownerId);
}