package com.pjg360.PJG360.model.dtos;

import com.pjg360.PJG360.enums.ReservationStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponseDTO {

    private Long id;
    private ReservationStatus status;

    // Info del ticket
    private Long ticketId;
    private String ticketType;
    private Double ticketPrice;
    private String homeTeam;
    private String awayTeam;
    private String matchDateTime;

    // Info del dueno
    private Long ownerId;
    private String ownerName;

    // Tiempos — HU3
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Long minutesRemaining;    // minutos restantes
    private String expiryMessage;     // mensaje de estado
}