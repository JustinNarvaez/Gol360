package com.pjg360.PJG360.services;

import com.pjg360.PJG360.enums.TicketType;
import com.pjg360.PJG360.model.dtos.*;

import java.util.List;
import java.util.Map;

public interface ITicketService {

    // Generar tickets para un partido
    Map<String, Object> generateTickets(Long matchId,
                                        int generalQty, int preferentialQty, int vipQty);

    // HU1: consultar disponibilidad
    List<TicketResponseDTO> getAvailableTickets(Long matchId);

    // HU6: ver mis entradas
    List<TicketResponseDTO> getTicketsByUser(Long userId);

    // Detalle de un ticket
    TicketResponseDTO getTicketById(Long ticketId);

    // HU2: reservar temporalmente
    ReservationResponseDTO reserveTicket(Long ticketId, Long userId);

    // HU3: ver reserva con tiempo restante
    ReservationResponseDTO getReservation(Long reservationId);

    // HU4: verificar y expirar si corresponde
    ReservationResponseDTO checkExpiry(Long reservationId);

    // HU5: confirmar compra con pago simulado
    PaymentResponseDTO confirmPurchase(Long reservationId, Long userId);
}