package com.pjg360.PJG360.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForecastRequestDTO {

    private Long localFanId;          // Usuario que predice
    private Long matchId;             // Partido a predecir
    private Integer predictedHomeGoals;
    private Integer predictedAwayGoals;
}