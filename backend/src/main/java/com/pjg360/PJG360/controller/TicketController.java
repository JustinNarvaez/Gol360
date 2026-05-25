package com.pjg360.PJG360.controller;

import com.pjg360.PJG360.model.dtos.*;
import com.pjg360.PJG360.services.ITicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pjg360/api")
public class TicketController {

    private final ITicketService ticketService;

    public TicketController(ITicketService ticketService) {
        this.ticketService = ticketService;
    }

    // Generar tickets para un partido
    @PostMapping("/tickets/generate/{matchId}")
    public ResponseEntity<?> generateTickets(
            @PathVariable Long matchId,
            @RequestParam(defaultValue = "10") int general,
            @RequestParam(defaultValue = "5") int preferential,
            @RequestParam(defaultValue = "3") int vip) {
        try {
            Map<String, Object> result = ticketService
                    .generateTickets(matchId, general, preferential, vip);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // HU1: consultar disponibilidad por partido
    @GetMapping("/tickets/match/{matchId}")
    public ResponseEntity<?> getAvailableTickets(
            @PathVariable Long matchId) {
        try {
            List<TicketResponseDTO> tickets =
                    ticketService.getAvailableTickets(matchId);

            if (tickets.isEmpty()) {
                return new ResponseEntity<>(
                        Map.of("message",
                                "No hay entradas disponibles para este partido"),
                        HttpStatus.OK);
            }
            return new ResponseEntity<>(tickets, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // HU6: ver mis entradas
    @GetMapping("/tickets/user/{userId}")
    public ResponseEntity<?> getTicketsByUser(
            @PathVariable Long userId) {
        try {
            List<TicketResponseDTO> tickets =
                    ticketService.getTicketsByUser(userId);

            if (tickets.isEmpty()) {
                return new ResponseEntity<>(
                        Map.of("message",
                                "No tienes entradas registradas"),
                        HttpStatus.OK);
            }
            return new ResponseEntity<>(tickets, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Detalle de un ticket
    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<?> getTicketById(
            @PathVariable Long ticketId) {
        try {
            return new ResponseEntity<>(
                    ticketService.getTicketById(ticketId), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // HU2: reservar temporalmente
    @PostMapping("/tickets/{ticketId}/reserve/{userId}")
    public ResponseEntity<?> reserveTicket(
            @PathVariable Long ticketId,
            @PathVariable Long userId) {
        try {
            ReservationResponseDTO reservation =
                    ticketService.reserveTicket(ticketId, userId);
            return new ResponseEntity<>(reservation, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // HU3: ver reserva con tiempo restante
    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<?> getReservation(
            @PathVariable Long reservationId) {
        try {
            return new ResponseEntity<>(
                    ticketService.getReservation(reservationId), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // HU4: verificar y expirar reserva
    @PostMapping("/reservations/{reservationId}/check-expiry")
    public ResponseEntity<?> checkExpiry(
            @PathVariable Long reservationId) {
        try {
            return new ResponseEntity<>(
                    ticketService.checkExpiry(reservationId), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // HU5: confirmar compra con pago simulado
    @PostMapping("/reservations/{reservationId}/confirm/{userId}")
    public ResponseEntity<?> confirmPurchase(
            @PathVariable Long reservationId,
            @PathVariable Long userId) {
        try {
            PaymentResponseDTO payment =
                    ticketService.confirmPurchase(reservationId, userId);
            return new ResponseEntity<>(payment, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}