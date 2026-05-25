package com.pjg360.PJG360.model.dtos;

import com.pjg360.PJG360.enums.PaymentStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {

    private Long id;
    private PaymentStatus status;
    private Double amount;
    private LocalDateTime paidAt;

    // Info del ticket
    private Long ticketId;
    private String homeTeam;
    private String awayTeam;
    private String matchDateTime;

    // Info del dueno
    private Long ownerId;
    private String ownerName;

    private String message;
}