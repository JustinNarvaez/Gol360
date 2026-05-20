package com.pjg360.PJG360.services.impl;

import com.pjg360.PJG360.enums.ForecastResult;
import com.pjg360.PJG360.enums.MatchStatus;
import com.pjg360.PJG360.enums.PollaStatus;
import com.pjg360.PJG360.model.dtos.*;
import com.pjg360.PJG360.model.entities.*;
import com.pjg360.PJG360.repositories.*;
import com.pjg360.PJG360.services.IPollaService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PollaServiceImpl implements IPollaService {

    private final PollaRepository pollaRepository;
    private final PollaGroupRepository pollaGroupRepository;
    private final ForecastRepository forecastRepository;
    private final RankingRepository rankingRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    public PollaServiceImpl(PollaRepository pollaRepository,
                            PollaGroupRepository pollaGroupRepository,
                            ForecastRepository forecastRepository,
                            RankingRepository rankingRepository,
                            MatchRepository matchRepository,
                            UserRepository userRepository) {
        this.pollaRepository = pollaRepository;
        this.pollaGroupRepository = pollaGroupRepository;
        this.forecastRepository = forecastRepository;
        this.rankingRepository = rankingRepository;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
    }

    @Override
    public PollaResponseDTO createPolla(PollaRequestDTO request) {
        // 1. Validar que el owner existe y es LocalFan
        User user = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new RuntimeException(
                        "Usuario con id " + request.getOwnerId() + " no encontrado"));

        if (!(user instanceof LocalFan)) {
            throw new RuntimeException(
                    "Solo los aficionados locales pueden crear pollas");
        }
        LocalFan owner = (LocalFan) user;

        // 2. Validar que los partidos existen
        List<Match> matches = new ArrayList<>();
        for (Long matchId : request.getMatchIds()) {
            Match match = matchRepository.findById(matchId)
                    .orElseThrow(() -> new RuntimeException(
                            "Partido con id " + matchId + " no encontrado"));
            matches.add(match);
        }

        // 3. Generar codigo unico del grupo
        String pollaCode = generateUniqueCode();

        // 4. Crear el grupo
        PollaGroup group = PollaGroup.builder()
                .name(request.getGroupName())
                .pollaCode(pollaCode)
                .totalPoints(0)
                .owner(owner)
                .fans(new ArrayList<>(List.of(owner)))
                .build();
        pollaGroupRepository.save(group);

        // 5. Crear la polla
        Polla polla = Polla.builder()
                .owner(owner)
                .matches(matches)
                .group(group)
                .status(PollaStatus.OPEN)
                .build();
        pollaRepository.save(polla);

        // 6. Crear ranking inicial para el owner
        Ranking ranking = Ranking.builder()
                .localFan(owner)
                .group(group)
                .points(0)
                .position(1)
                .build();
        rankingRepository.save(ranking);

        return toResponseDTO(polla);
    }

    @Override
    public List<PollaResponseDTO> getAllPollas() {
        return pollaRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PollaResponseDTO getPollaById(Long pollaId) {
        Polla polla = pollaRepository.findById(pollaId)
                .orElseThrow(() -> new RuntimeException(
                        "Polla con id " + pollaId + " no encontrada"));
        return toResponseDTO(polla);
    }

    @Override
    public PollaResponseDTO joinPolla(String pollaCode, Long localFanId) {
        // 1. Buscar grupo por codigo
        PollaGroup group = pollaGroupRepository.findByPollaCode(pollaCode)
                .orElseThrow(() -> new RuntimeException(
                        "Codigo de polla invalido: " + pollaCode));

        // 2. Buscar la polla asociada al grupo
        Polla polla = pollaRepository.findAll().stream()
                .filter(p -> p.getGroup().getId().equals(group.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Polla no encontrada"));

        // 3. Validar que la polla esta abierta
        if (polla.getStatus() != PollaStatus.OPEN) {
            throw new RuntimeException(
                    "Esta polla ya no acepta nuevos participantes");
        }

        // 4. Validar usuario
        User user = userRepository.findById(localFanId)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario con id " + localFanId + " no encontrado"));

        if (!(user instanceof LocalFan)) {
            throw new RuntimeException(
                    "Solo los aficionados locales pueden unirse a pollas");
        }
        LocalFan fan = (LocalFan) user;

        // 5. Verificar que no esta ya en el grupo
        boolean alreadyIn = group.getFans().stream()
                .anyMatch(f -> f.getId().equals(localFanId));
        if (alreadyIn) {
            throw new RuntimeException(
                    "El usuario ya esta participando en esta polla");
        }

        // 6. Agregar al grupo
        group.getFans().add(fan);
        pollaGroupRepository.save(group);

        // 7. Crear entrada en ranking
        Ranking ranking = Ranking.builder()
                .localFan(fan)
                .group(group)
                .points(0)
                .position(group.getFans().size())
                .build();
        rankingRepository.save(ranking);

        return toResponseDTO(polla);
    }

    @Override
    public ForecastResponseDTO createForecast(Long pollaId,
                                              ForecastRequestDTO request) {
        // 1. Validar polla
        Polla polla = pollaRepository.findById(pollaId)
                .orElseThrow(() -> new RuntimeException(
                        "Polla con id " + pollaId + " no encontrada"));

        if (polla.getStatus() != PollaStatus.OPEN) {
            throw new RuntimeException(
                    "Esta polla no acepta mas predicciones");
        }

        // 2. Validar que el partido pertenece a la polla
        boolean matchInPolla = polla.getMatches().stream()
                .anyMatch(m -> m.getId().equals(request.getMatchId()));
        if (!matchInPolla) {
            throw new RuntimeException(
                    "El partido no pertenece a esta polla");
        }

        // 3. Validar partido no iniciado
        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new RuntimeException(
                        "Partido con id " + request.getMatchId() + " no encontrado"));

        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new RuntimeException(
                    "Solo se pueden predecir partidos que aun no han iniciado");
        }

        // 4. Validar usuario
        User user = userRepository.findById(request.getLocalFanId())
                .orElseThrow(() -> new RuntimeException(
                        "Usuario con id " + request.getLocalFanId() + " no encontrado"));

        if (!(user instanceof LocalFan)) {
            throw new RuntimeException(
                    "Solo los aficionados locales pueden hacer predicciones");
        }
        LocalFan fan = (LocalFan) user;

        // 5. Validar que el usuario esta en la polla
        boolean inPolla = polla.getGroup().getFans().stream()
                .anyMatch(f -> f.getId().equals(request.getLocalFanId()));
        if (!inPolla) {
            throw new RuntimeException(
                    "El usuario no pertenece a esta polla");
        }

        // 6. Verificar que no haya prediccion duplicada
        if (forecastRepository.existsByPollaIdAndLocalFanIdAndMatchId(
                pollaId, request.getLocalFanId(), request.getMatchId())) {
            throw new RuntimeException(
                    "Ya existe una prediccion para este partido en esta polla");
        }

        // 7. Calcular resultado predicho
        ForecastResult result = calculateForecastResult(
                request.getPredictedHomeGoals(),
                request.getPredictedAwayGoals());

        // 8. Guardar prediccion
        Forecast forecast = Forecast.builder()
                .polla(polla)
                .match(match)
                .localFan(fan)
                .predictedHomeGoals(request.getPredictedHomeGoals())
                .predictedAwayGoals(request.getPredictedAwayGoals())
                .predictedResult(result)
                .submittedAt(LocalDateTime.now())
                .pointsEarned(0)
                .build();

        forecastRepository.save(forecast);
        return toForecastDTO(forecast);
    }

    @Override
    public List<ForecastResponseDTO> getForecastsByUser(Long pollaId,
                                                        Long localFanId) {
        return forecastRepository
                .findByPollaIdAndLocalFanId(pollaId, localFanId)
                .stream()
                .map(this::toForecastDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RankingResponseDTO> getRankingByPolla(Long pollaId) {
        Polla polla = pollaRepository.findById(pollaId)
                .orElseThrow(() -> new RuntimeException(
                        "Polla con id " + pollaId + " no encontrada"));

        return rankingRepository
                .findByGroupIdOrderByPointsDesc(polla.getGroup().getId())
                .stream()
                .map(this::toRankingDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> calculatePoints(Long pollaId) {
        Polla polla = pollaRepository.findById(pollaId)
                .orElseThrow(() -> new RuntimeException(
                        "Polla con id " + pollaId + " no encontrada"));

        int totalCalculated = 0;
        int totalSkipped = 0;

        // Iterar sobre todas las predicciones de la polla
        for (Match match : polla.getMatches()) {
            // Solo calcular partidos FINALIZADOS con score
            if (match.getStatus() != MatchStatus.FINISHED) {
                totalSkipped++;
                continue;
            }
            if (match.getHomeScore() == null || match.getAwayScore() == null) {
                totalSkipped++;
                continue;
            }

            // Resultado real del partido
            ForecastResult realResult = calculateForecastResult(
                    match.getHomeScore(), match.getAwayScore());

            // Predicciones de este partido
            List<Forecast> forecasts = forecastRepository
                    .findByPollaIdAndMatchId(pollaId, match.getId());

            for (Forecast forecast : forecasts) {
                int points = 0;

// Marcador exacto → 3 puntos
                if (forecast.getPredictedHomeGoals().equals(match.getHomeScore())
                        && forecast.getPredictedAwayGoals().equals(match.getAwayScore())) {
                    points = 3;
                }
                else if (forecast.getPredictedResult() == realResult) {
                    points = 1;
                }

// ← Variable final para usar dentro del lambda
                final int earnedPoints = points;

                forecast.setPointsEarned(earnedPoints);
                forecastRepository.save(forecast);

                rankingRepository
                        .findByGroupIdAndLocalFanId(
                                polla.getGroup().getId(),
                                forecast.getLocalFan().getId())
                        .ifPresent(ranking -> {
                            ranking.setPoints(ranking.getPoints() + earnedPoints); // ← usa earnedPoints
                            rankingRepository.save(ranking);
                        });

                totalCalculated++;
            }
        }

        // Recalcular posiciones del ranking
        List<Ranking> rankings = rankingRepository
                .findByGroupIdOrderByPointsDesc(polla.getGroup().getId());
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setPosition(i + 1);
            rankingRepository.save(rankings.get(i));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("pollaId", pollaId);
        response.put("predictionsCalculated", totalCalculated);
        response.put("matchesPending", totalSkipped);
        response.put("message", "Puntos calculados exitosamente");
        response.put("ranking", getRankingByPolla(pollaId));
        return response;
    }

    @Override
    public PollaResponseDTO closePolla(Long pollaId) {
        Polla polla = pollaRepository.findById(pollaId)
                .orElseThrow(() -> new RuntimeException(
                        "Polla con id " + pollaId + " no encontrada"));

        if (polla.getStatus() != PollaStatus.OPEN) {
            throw new RuntimeException(
                    "Solo se pueden cerrar pollas en estado OPEN");
        }

        polla.setStatus(PollaStatus.CLOSED);
        return toResponseDTO(pollaRepository.save(polla));
    }

    // ─── Metodos privados de apoyo ───────────────────────────────────────

    private ForecastResult calculateForecastResult(int home, int away) {
        if (home > away) return ForecastResult.HOME_WIN;
        if (away > home) return ForecastResult.AWAY_WIN;
        return ForecastResult.DRAW;
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (pollaGroupRepository.existsByPollaCode(code));
        return code;
    }

    private PollaResponseDTO toResponseDTO(Polla polla) {
        PollaGroup group = polla.getGroup();
        return PollaResponseDTO.builder()
                .id(polla.getId())
                .status(polla.getStatus())
                .ownerId(polla.getOwner().getId())
                .ownerName(polla.getOwner().getFirstName()
                        + " " + polla.getOwner().getLastName())
                .groupId(group.getId())
                .groupName(group.getName())
                .pollaCode(group.getPollaCode())
                .totalFans(group.getFans() != null ? group.getFans().size() : 0)
                .matchIds(polla.getMatches().stream()
                        .map(Match::getId)
                        .collect(Collectors.toList()))
                .totalMatches(polla.getMatches().size())
                .build();
    }

    private ForecastResponseDTO toForecastDTO(Forecast f) {
        return ForecastResponseDTO.builder()
                .id(f.getId())
                .pollaId(f.getPolla().getId())
                .matchId(f.getMatch().getId())
                .homeTeam(f.getMatch().getHomeTeam() != null
                        ? f.getMatch().getHomeTeam().getName() : "TBD")
                .awayTeam(f.getMatch().getAwayTeam() != null
                        ? f.getMatch().getAwayTeam().getName() : "TBD")
                .matchDateTime(f.getMatch().getDateTime() != null
                        ? f.getMatch().getDateTime().toString() : null)
                .localFanId(f.getLocalFan().getId())
                .localFanName(f.getLocalFan().getFirstName()
                        + " " + f.getLocalFan().getLastName())
                .predictedHomeGoals(f.getPredictedHomeGoals())
                .predictedAwayGoals(f.getPredictedAwayGoals())
                .predictedResult(f.getPredictedResult())
                .actualHomeGoals(f.getMatch().getHomeScore())
                .actualAwayGoals(f.getMatch().getAwayScore())
                .pointsEarned(f.getPointsEarned())
                .submittedAt(f.getSubmittedAt())
                .build();
    }

    private RankingResponseDTO toRankingDTO(Ranking r) {
        return RankingResponseDTO.builder()
                .position(r.getPosition())
                .localFanId(r.getLocalFan().getId())
                .localFanName(r.getLocalFan().getFirstName()
                        + " " + r.getLocalFan().getLastName())
                .points(r.getPoints())
                .groupId(r.getGroup().getId())
                .groupName(r.getGroup().getName())
                .build();
    }
}