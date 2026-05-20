package com.pjg360.PJG360.services;

import com.pjg360.PJG360.model.dtos.*;

import java.util.List;
import java.util.Map;

public interface IPollaService {

    // Crear una polla nueva
    PollaResponseDTO createPolla(PollaRequestDTO request);

    // Listar todas las pollas
    List<PollaResponseDTO> getAllPollas();

    // Detalle de una polla
    PollaResponseDTO getPollaById(Long pollaId);

    // Unirse a una polla con el codigo del grupo
    PollaResponseDTO joinPolla(String pollaCode, Long localFanId);

    // Hacer una prediccion
    ForecastResponseDTO createForecast(Long pollaId, ForecastRequestDTO request);

    // Ver predicciones de un usuario en una polla
    List<ForecastResponseDTO> getForecastsByUser(Long pollaId, Long localFanId);

    // Ver ranking de una polla
    List<RankingResponseDTO> getRankingByPolla(Long pollaId);

    // Calcular puntos (llamar despues de que partidos terminen)
    Map<String, Object> calculatePoints(Long pollaId);

    // Cerrar polla (no acepta mas predicciones)
    PollaResponseDTO closePolla(Long pollaId);
}