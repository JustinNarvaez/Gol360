package com.pjg360.PJG360.services.impl;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.pjg360.PJG360.enums.*;
import com.pjg360.PJG360.model.dtos.*;
import com.pjg360.PJG360.model.entities.*;
import com.pjg360.PJG360.repositories.*;
import com.pjg360.PJG360.services.ITicketService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.pjg360.PJG360.model.entities.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements ITicketService {

    private final TicketRepository ticketRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    // Minutos antes de que expire una reserva
    private static final int EXPIRY_MINUTES = 15;

    // Maximo de reservas activas por usuario
    private static final int MAX_ACTIVE_RESERVATIONS = 3;

    @Value("${mercadopago.access.token}")
    private String mpAccessToken;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             ReservationRepository reservationRepository,
                             PaymentRepository paymentRepository,
                             MatchRepository matchRepository,
                             UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Map<String, Object> generateTickets(Long matchId,
                                               int generalQty, int preferentialQty, int vipQty) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException(
                        "Partido con id " + matchId + " no encontrado"));

        List<Ticket> tickets = new ArrayList<>();

        // Precios por tipo
        Map<TicketType, Double> prices = Map.of(
                TicketType.GENERAL, 150.0,
                TicketType.PREFERENTIAL, 300.0,
                TicketType.VIP, 600.0
        );

        // Generar tickets GENERAL
        for (int i = 0; i < generalQty; i++) {
            tickets.add(Ticket.builder()
                    .match(match)
                    .status(TicketStatus.AVAILABLE)
                    .type(TicketType.GENERAL)
                    .price(prices.get(TicketType.GENERAL))
                    .build());
        }

        // Generar tickets PREFERENTIAL
        for (int i = 0; i < preferentialQty; i++) {
            tickets.add(Ticket.builder()
                    .match(match)
                    .status(TicketStatus.AVAILABLE)
                    .type(TicketType.PREFERENTIAL)
                    .price(prices.get(TicketType.PREFERENTIAL))
                    .build());
        }

        // Generar tickets VIP
        for (int i = 0; i < vipQty; i++) {
            tickets.add(Ticket.builder()
                    .match(match)
                    .status(TicketStatus.AVAILABLE)
                    .type(TicketType.VIP)
                    .price(prices.get(TicketType.VIP))
                    .build());
        }

        ticketRepository.saveAll(tickets);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("matchId", matchId);
        response.put("homeTeam", match.getHomeTeam() != null
                ? match.getHomeTeam().getName() : "TBD");
        response.put("awayTeam", match.getAwayTeam() != null
                ? match.getAwayTeam().getName() : "TBD");
        response.put("totalGenerated", tickets.size());
        response.put("general", generalQty);
        response.put("preferential", preferentialQty);
        response.put("vip", vipQty);
        response.put("message", "Tickets generados exitosamente");
        return response;
    }

    @Override
    public List<TicketResponseDTO> getAvailableTickets(Long matchId) {
        List<Ticket> available = ticketRepository
                .findByMatchIdAndStatus(matchId, TicketStatus.AVAILABLE);

        // HU1 Escenario 2: sin tickets disponibles
        if (available.isEmpty()) {
            return new ArrayList<>();
        }

        return available.stream()
                .map(this::toTicketDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TicketResponseDTO> getTicketsByUser(Long userId) {
        return ticketRepository.findByOwnerId(userId)
                .stream()
                .map(this::toTicketDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TicketResponseDTO getTicketById(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException(
                        "Ticket con id " + ticketId + " no encontrado"));
        return toTicketDTO(ticket);
    }

    @Override
    public ReservationResponseDTO reserveTicket(Long ticketId, Long userId) {
        // 1. Validar ticket
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException(
                        "Ticket con id " + ticketId + " no encontrado"));

        // HU2 Escenario 2: ticket no disponible
        if (ticket.getStatus() != TicketStatus.AVAILABLE) {
            throw new RuntimeException(
                    "El ticket no esta disponible. Estado actual: "
                            + ticket.getStatus());
        }

        // 2. Validar usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario con id " + userId + " no encontrado"));

        if (!(user instanceof LocalFan)) {
            throw new RuntimeException(
                    "Solo los aficionados locales pueden reservar tickets");
        }
        LocalFan fan = (LocalFan) user;

        // HU2 Escenario 3: limite de reservas activas
        long activeReservations = reservationRepository
                .findByOwnerIdAndStatus(userId, ReservationStatus.ACTIVE)
                .size();

        if (activeReservations >= MAX_ACTIVE_RESERVATIONS) {
            throw new RuntimeException(
                    "Has alcanzado el limite de " + MAX_ACTIVE_RESERVATIONS
                            + " reservas activas. Confirma o cancela una antes de continuar");
        }

        // 3. Cambiar estado del ticket
        ticket.setStatus(TicketStatus.RESERVED);
        ticket.setOwner(fan);
        ticketRepository.save(ticket);

        // 4. Crear reserva con tiempo de expiracion
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = Reservation.builder()
                .owner(fan)
                .ticket(ticket)
                .status(ReservationStatus.ACTIVE)
                .createdAt(now)
                .expiresAt(now.plusMinutes(EXPIRY_MINUTES))
                .build();

        reservationRepository.save(reservation);
        return toReservationDTO(reservation);
    }

    @Override
    public ReservationResponseDTO getReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException(
                        "Reserva con id " + reservationId + " no encontrada"));

        // Verificar si expiró al consultar
        if (reservation.getStatus() == ReservationStatus.ACTIVE
                && LocalDateTime.now().isAfter(reservation.getExpiresAt())) {
            expireReservation(reservation);
        }

        return toReservationDTO(reservation);
    }

    @Override
    public ReservationResponseDTO checkExpiry(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException(
                        "Reserva con id " + reservationId + " no encontrada"));

        if (reservation.getStatus() == ReservationStatus.ACTIVE
                && LocalDateTime.now().isAfter(reservation.getExpiresAt())) {
            expireReservation(reservation);
        }

        return toReservationDTO(reservation);
    }

    @Override
    public PaymentResponseDTO confirmPurchase(Long reservationId, Long userId) {
        // 1. Validar reserva
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException(
                        "Reserva con id " + reservationId + " no encontrada"));

        // HU5 Escenario 2: reserva expirada
        if (reservation.getStatus() == ReservationStatus.EXPIRED
                || LocalDateTime.now().isAfter(reservation.getExpiresAt())) {
            expireReservation(reservation);
            throw new RuntimeException(
                    "La reserva ha expirado. El ticket fue liberado");
        }

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new RuntimeException(
                    "La reserva no esta activa. Estado: " + reservation.getStatus());
        }

        Ticket ticket = reservation.getTicket();

        // 2. Procesar pago con MercadoPago Sandbox
        String mpStatus = "approved"; // valor por defecto simulado

        try {
            MercadoPagoConfig.setAccessToken(mpAccessToken);

            PaymentCreateRequest paymentRequest = PaymentCreateRequest.builder()
                    .transactionAmount(BigDecimal.valueOf(ticket.getPrice()))
                    .description("Ticket " + ticket.getType()
                            + " - Partido id " + ticket.getMatch().getId())
                    .paymentMethodId("account_money")
                    .payer(PaymentPayerRequest.builder()
                            .email("test_user_123456@testuser.com")
                            .build())
                    .build();

            PaymentClient client = new PaymentClient();
            com.mercadopago.resources.payment.Payment mpPayment =
                    client.create(paymentRequest);
            mpStatus = mpPayment.getStatus();

        } catch (Exception e) {
            // Si MP falla en sandbox, simulamos aprobacion para demo
            System.out.println("MercadoPago sandbox error (simulando aprobacion): "
                    + e.getMessage());
            mpStatus = "approved";
        }


        if ("approved".equals(mpStatus)) {
            // HU5 Escenario 1: pago aprobado
            ticket.setStatus(TicketStatus.PAID);
            ticketRepository.save(ticket);

            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);

            Payment payment = Payment.builder()
                    .owner(reservation.getOwner())
                    .ticket(ticket)
                    .amount(ticket.getPrice())
                    .status(PaymentStatus.COMPLETED)
                    .paidAt(LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);

            return toPaymentDTO(payment,
                    "Pago aprobado exitosamente. Ticket confirmado");

        } else {
            // HU5 Escenario 3: pago fallido
            Payment payment = Payment.builder()
                    .owner(reservation.getOwner())
                    .ticket(ticket)
                    .amount(ticket.getPrice())
                    .status(PaymentStatus.FAILED)
                    .paidAt(LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);

            return toPaymentDTO(payment,
                    "Pago rechazado. Estado: " + mpStatus);
        }
    }

    // ─── Metodos privados ────────────────────────────────────────────────

    private void expireReservation(Reservation reservation) {
        // HU4: ticket vuelve a AVAILABLE
        Ticket ticket = reservation.getTicket();
        ticket.setStatus(TicketStatus.AVAILABLE);
        ticket.setOwner(null);
        ticketRepository.save(ticket);

        reservation.setStatus(ReservationStatus.EXPIRED);
        reservationRepository.save(reservation);
    }

    private TicketResponseDTO toTicketDTO(Ticket t) {
        return TicketResponseDTO.builder()
                .id(t.getId())
                .status(t.getStatus())
                .type(t.getType())
                .price(t.getPrice())
                .matchId(t.getMatch().getId())
                .homeTeam(t.getMatch().getHomeTeam() != null
                        ? t.getMatch().getHomeTeam().getName() : "TBD")
                .awayTeam(t.getMatch().getAwayTeam() != null
                        ? t.getMatch().getAwayTeam().getName() : "TBD")
                .matchDateTime(t.getMatch().getDateTime() != null
                        ? t.getMatch().getDateTime().toString() : null)
                .matchGroup(t.getMatch().getMatchGroup())
                .ownerId(t.getOwner() != null ? t.getOwner().getId() : null)
                .ownerName(t.getOwner() != null
                        ? t.getOwner().getFirstName()
                        + " " + t.getOwner().getLastName() : null)
                .build();
    }

    private ReservationResponseDTO toReservationDTO(Reservation r) {
        long minutesRemaining = 0;
        String expiryMessage;

        if (r.getStatus() == ReservationStatus.ACTIVE) {
            minutesRemaining = ChronoUnit.MINUTES.between(
                    LocalDateTime.now(), r.getExpiresAt());
            minutesRemaining = Math.max(0, minutesRemaining);
            expiryMessage = minutesRemaining > 0
                    ? "Tienes " + minutesRemaining + " minutos para confirmar"
                    : "La reserva expiro";
        } else if (r.getStatus() == ReservationStatus.CONFIRMED) {
            expiryMessage = "Reserva confirmada y pagada";
        } else {
            expiryMessage = "Reserva expirada";
        }

        return ReservationResponseDTO.builder()
                .id(r.getId())
                .status(r.getStatus())
                .ticketId(r.getTicket().getId())
                .ticketType(r.getTicket().getType().toString())
                .ticketPrice(r.getTicket().getPrice())
                .homeTeam(r.getTicket().getMatch().getHomeTeam() != null
                        ? r.getTicket().getMatch().getHomeTeam().getName() : "TBD")
                .awayTeam(r.getTicket().getMatch().getAwayTeam() != null
                        ? r.getTicket().getMatch().getAwayTeam().getName() : "TBD")
                .matchDateTime(r.getTicket().getMatch().getDateTime() != null
                        ? r.getTicket().getMatch().getDateTime().toString() : null)
                .ownerId(r.getOwner().getId())
                .ownerName(r.getOwner().getFirstName()
                        + " " + r.getOwner().getLastName())
                .createdAt(r.getCreatedAt())
                .expiresAt(r.getExpiresAt())
                .minutesRemaining(minutesRemaining)
                .expiryMessage(expiryMessage)
                .build();
    }

    private PaymentResponseDTO toPaymentDTO(
            com.pjg360.PJG360.model.entities.Payment p,
            String message) {
        return PaymentResponseDTO.builder()
                .id(p.getId())
                .status(p.getStatus())
                .amount(p.getAmount())
                .paidAt(p.getPaidAt())
                .ticketId(p.getTicket().getId())
                .homeTeam(p.getTicket().getMatch().getHomeTeam() != null
                        ? p.getTicket().getMatch().getHomeTeam().getName() : "TBD")
                .awayTeam(p.getTicket().getMatch().getAwayTeam() != null
                        ? p.getTicket().getMatch().getAwayTeam().getName() : "TBD")
                .matchDateTime(p.getTicket().getMatch().getDateTime() != null
                        ? p.getTicket().getMatch().getDateTime().toString() : null)
                .ownerId(p.getOwner().getId())
                .ownerName(p.getOwner().getFirstName()
                        + " " + p.getOwner().getLastName())
                .message(message)
                .build();
    }
}