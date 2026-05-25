package com.pjg360.PJG360.model.dtos;

import com.pjg360.PJG360.enums.TicketStatus;
import com.pjg360.PJG360.enums.TicketType;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDTO {

    private Long id;
    private TicketStatus status;
    private TicketType type;
    private Double price;

    // Info del partido
    private Long matchId;
    private String homeTeam;
    private String awayTeam;
    private String matchDateTime;
    private String matchGroup;

    // Info del dueno (si tiene)
    private Long ownerId;
    private String ownerName;
}