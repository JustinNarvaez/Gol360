package com.pjg360.PJG360.model.dtos;

import com.pjg360.PJG360.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchEventResponseDTO {
    private Long id;
    private EventType event;
    private Integer eventMinute;

    // Info del jugador (sin exponer la entidad completa)
    private Long playerId;
    private String playerName;
    private String playerPosition;

    // Referencia al partido
    private Long matchId;
}
