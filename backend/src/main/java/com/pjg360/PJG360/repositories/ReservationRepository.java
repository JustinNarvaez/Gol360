package com.pjg360.PJG360.repositories;

import com.pjg360.PJG360.enums.ReservationStatus;
import com.pjg360.PJG360.model.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Reservas activas de un usuario
    List<Reservation> findByOwnerIdAndStatus(Long ownerId, ReservationStatus status);

    // Reservas expiradas que aun no se procesaron
    List<Reservation> findByStatusAndExpiresAtBefore(
            ReservationStatus status, LocalDateTime now);

    // Reserva por ticket
    java.util.Optional<Reservation> findByTicketId(Long ticketId);
}