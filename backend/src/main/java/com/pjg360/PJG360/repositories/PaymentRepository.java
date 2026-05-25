package com.pjg360.PJG360.repositories;

import com.pjg360.PJG360.model.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOwnerId(Long ownerId);
    java.util.Optional<Payment> findByTicketId(Long ticketId);
}