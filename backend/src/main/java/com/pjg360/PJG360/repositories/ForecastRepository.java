package com.pjg360.PJG360.repositories;

import com.pjg360.PJG360.model.entities.Forecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForecastRepository extends JpaRepository<Forecast, Long> {

    // Predicciones de un usuario en una polla
    List<Forecast> findByPollaIdAndLocalFanId(Long pollaId, Long localFanId);

    // Prediccion especifica de un usuario para un partido en una polla
    Optional<Forecast> findByPollaIdAndLocalFanIdAndMatchId(
            Long pollaId, Long localFanId, Long matchId);

    // Todas las predicciones de un partido en una polla
    List<Forecast> findByPollaIdAndMatchId(Long pollaId, Long matchId);

    // Verificar si un usuario ya predijo un partido
    boolean existsByPollaIdAndLocalFanIdAndMatchId(
            Long pollaId, Long localFanId, Long matchId);
}