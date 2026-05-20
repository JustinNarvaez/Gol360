package com.pjg360.PJG360.model.dtos;

import com.pjg360.PJG360.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchEventRequestDTO {
    // Tipo de evento: GOAL, YELLOW_CARD, RED_CARD, SUBSTITUTION
    private EventType event;

    // Minuto del partido (1-120 normalmente)
    private Integer eventMinute;

    // ID del jugador involucrado (opcional, puede ser null)
    private Long playerId;
}
