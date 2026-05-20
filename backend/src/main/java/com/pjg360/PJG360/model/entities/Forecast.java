package com.pjg360.PJG360.model.entities;

import com.pjg360.PJG360.enums.ForecastResult;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "forecasts")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Forecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer predictedHomeGoals;
    private Integer predictedAwayGoals;

    @Enumerated(EnumType.STRING)
    private ForecastResult predictedResult;

    // Partido al que pertenece la prediccion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    // Usuario que hizo la prediccion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_fan_id", nullable = false)
    private LocalFan localFan;

    // Polla a la que pertenece
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "polla_id", nullable = false)
    private Polla polla;

    private LocalDateTime submittedAt;

    // Puntos obtenidos (se calcula despues del partido)
    private Integer pointsEarned;
}