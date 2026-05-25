package com.pjg360.PJG360.repositories;

import com.pjg360.PJG360.enums.TicketStatus;
import com.pjg360.PJG360.model.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // HU1: disponibilidad por partido
    List<Ticket> findByMatchIdAndStatus(Long matchId, TicketStatus status);

    // Todos los tickets de un partido
    List<Ticket> findByMatchId(Long matchId);

    // HU6: tickets de un usuario
    List<Ticket> findByOwnerId(Long ownerId);

    // Contar reservas activas del dia (HU2 Escenario 3)
    long countByOwnerIdAndStatus(Long ownerId, TicketStatus status);
}