package com.pjg360.PJG360.model.dtos;

import com.pjg360.PJG360.enums.ForecastResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastResponseDTO {

    private Long id;
    private Long pollaId;
    private Long matchId;
    private String homeTeam;
    private String awayTeam;
    private String matchDateTime;

    // Prediccion del usuario
    private Long localFanId;
    private String localFanName;
    private Integer predictedHomeGoals;
    private Integer predictedAwayGoals;
    private ForecastResult predictedResult;

    // Resultado real (si el partido ya termino)
    private Integer actualHomeGoals;
    private Integer actualAwayGoals;

    // Puntos obtenidos
    private Integer pointsEarned;
    private LocalDateTime submittedAt;
}