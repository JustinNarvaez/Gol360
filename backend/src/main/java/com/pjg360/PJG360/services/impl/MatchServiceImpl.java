package com.pjg360.PJG360.services.impl;

import com.pjg360.PJG360.enums.MatchStatus;
import com.pjg360.PJG360.enums.TournamentPhase;
import com.pjg360.PJG360.model.dtos.MatchResponseDTO;
import com.pjg360.PJG360.model.dtos.MatchStatusUpdateDTO;
import com.pjg360.PJG360.model.entities.Match;
import com.pjg360.PJG360.model.entities.Team;
import com.pjg360.PJG360.repositories.MatchRepository;
import com.pjg360.PJG360.repositories.TeamRepository;
import com.pjg360.PJG360.services.IMatchService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.pjg360.PJG360.model.dtos.StandingsDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchServiceImpl implements IMatchService {
    private static final String API_URL =
            "https://raw.githubusercontent.com/openfootball/worldcup.json/master/2026/worldcup.json";

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MatchServiceImpl(MatchRepository matchRepository,
                            TeamRepository teamRepository,
                            RestTemplate restTemplate,
                            ObjectMapper objectMapper) {
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String loadMatchesFromApi() {
        try {
            System.out.println("Cargando partidos desde openfootball...");
            String json = restTemplate.getForObject(API_URL, String.class);
            JsonNode root = objectMapper.readTree(json);

            JsonNode matchesNode = root.get("matches");
            if (matchesNode == null) return "Error: no se encontró el campo 'matches'";

            matchRepository.deleteAll();
            List<Match> matches = new ArrayList<>();

            for (JsonNode m : matchesNode) {
                Match match = new Match();

                // Round y fase
                String roundName = m.has("round") ? m.get("round").asText() : "Unknown";
                match.setRoundName(roundName);
                match.setPhase(detectPhase(roundName));
                match.setRefreshed(true);

                if (m.has("group")) {
                    match.setMatchGroup(m.get("group").asText());
                }

                // Fecha y hora — tiempo viene como "13:00 UTC-6"
                if (m.has("date")) {
                    try {
                        LocalDate date = LocalDate.parse(m.get("date").asText());
                        LocalTime time = LocalTime.of(0, 0);
                        if (m.has("time")) {
                            String timeStr = m.get("time").asText();
                            // Quitar el timezone: "13:00 UTC-6" → "13:00"
                            String cleanTime = timeStr.split(" ")[0];
                            time = LocalTime.parse(cleanTime);
                        }
                        match.setDateTime(LocalDateTime.of(date, time));
                    } catch (Exception e) {
                        match.setDateTime(null);
                    }
                }

                // Equipos — ahora son strings con el nombre
                if (m.has("team1")) {
                    String name = m.get("team1").asText();
                    teamRepository.findByName(name).ifPresent(match::setHomeTeam);
                }
                if (m.has("team2")) {
                    String name = m.get("team2").asText();
                    teamRepository.findByName(name).ifPresent(match::setAwayTeam);
                }

                if (m.has("team1")) {
                    String name = m.get("team1").asText();
                    Optional<Team> team = teamRepository.findByName(name);
                    if (team.isPresent()) {
                        match.setHomeTeam(team.get());
                    } else {
                        // Crea un Team temporal solo con el nombre
                        Team temp = new Team();
                        temp.setName(name);
                        match.setHomeTeam(teamRepository.save(temp));
                    }
                }

                // Score si existe
                if (m.has("score")) {
                    JsonNode score = m.get("score");
                    if (score.has("ft")) {
                        match.setHomeScore(score.get("ft").get(0).asInt());
                        match.setAwayScore(score.get("ft").get(1).asInt());
                        match.setStatus(MatchStatus.FINISHED);
                    } else {
                        match.setStatus(MatchStatus.SCHEDULED);
                    }
                } else {
                    match.setStatus(MatchStatus.SCHEDULED);
                }

                matches.add(match);
            }

            matchRepository.saveAll(matches);
            return "Se cargaron " + matches.size() + " partidos del Mundial 2026";

        } catch (Exception e) {
            return "Error al cargar partidos: " + e.getMessage();
        }
    }

    @Override
    public List<MatchResponseDTO> getAllMatches() {
        return matchRepository.findAll().stream().map(this::toDTO).toList();
    }



    @Override
    public List<MatchResponseDTO> getMatchesByPhase(TournamentPhase phase) {
        return matchRepository.findByPhase(phase).stream().map(this::toDTO).toList();
    }

    @Override
    public List<MatchResponseDTO> getMatchesByTeam(String fifaCode) {
        return matchRepository
                .findByHomeTeamFifaCodeOrAwayTeamFifaCode(fifaCode, fifaCode)
                .stream().map(this::toDTO).toList();
    }

    @Override
    public List<MatchResponseDTO> getMatchesByRound(String roundName) {
        return matchRepository.findByRoundName(roundName).stream().map(this::toDTO).toList();
    }

    @Override
    public List<MatchResponseDTO> getScheduledMatches() {
        return matchRepository.findByStatus(MatchStatus.SCHEDULED).stream().map(this::toDTO).toList();
    }

    @Override
    public List<MatchResponseDTO> getFinishedMatches() {
        return matchRepository.findByStatus(MatchStatus.FINISHED).stream().map(this::toDTO).toList();
    }

    @Override
    public MatchResponseDTO getById(Long id) {
        return matchRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Partido no encontrado"));
    }

    @Override
    public MatchResponseDTO updateMatchStatus(Long matchId, MatchStatusUpdateDTO request) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException(
                        "Partido con id " + matchId + " no encontrado"));

        MatchStatus currentStatus = match.getStatus();
        MatchStatus newStatus = request.getStatus();

        // Validaciones de transicion
        if (currentStatus == MatchStatus.FINISHED) {
            throw new RuntimeException(
                    "No se puede cambiar el estado de un partido finalizado");
        }
        if (currentStatus == MatchStatus.SCHEDULED
                && newStatus == MatchStatus.FINISHED) {
            throw new RuntimeException(
                    "Un partido no puede pasar de SCHEDULED a FINISHED directamente");
        }
        if (currentStatus == newStatus) {
            throw new RuntimeException(
                    "El partido ya se encuentra en estado " + newStatus);
        }

        // Si se finaliza, el marcador es obligatorio
        if (newStatus == MatchStatus.FINISHED) {
            if (request.getHomeScore() == null || request.getAwayScore() == null) {
                throw new RuntimeException(
                        "Para finalizar un partido debes ingresar el marcador " +
                                "(homeScore y awayScore)");
            }
            if (request.getHomeScore() < 0 || request.getAwayScore() < 0) {
                throw new RuntimeException(
                        "El marcador no puede ser negativo");
            }
            match.setHomeScore(request.getHomeScore());
            match.setAwayScore(request.getAwayScore());
        }

        match.setStatus(newStatus);
        match.setRefreshed(true);
        Match updated = matchRepository.save(match);
        return toDTO(updated);
    }



    @Override
    public Map<String, Object> getResultsByRound(String roundName) {
        List<Match> allMatches = matchRepository
                .findByRoundNameOrderByDateTimeAsc(roundName);

        if (allMatches.isEmpty()) {
            throw new RuntimeException(
                    "No se encontraron partidos para la jornada: " + roundName);
        }

        List<MatchResponseDTO> finished = allMatches.stream()
                .filter(m -> m.getStatus() == MatchStatus.FINISHED)
                .map(this::toDTO)
                .collect(Collectors.toList());

        List<MatchResponseDTO> pending = allMatches.stream()
                .filter(m -> m.getStatus() != MatchStatus.FINISHED)
                .map(this::toDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("round", roundName);
        response.put("totalMatches", allMatches.size());
        response.put("finishedCount", finished.size());
        response.put("pendingCount", pending.size());

        // HU4 - Escenario 2: fecha en curso
        if (!pending.isEmpty() && !finished.isEmpty()) {
            response.put("message",
                    "Jornada en curso. " + pending.size() +
                            " partido(s) aun pendiente(s) de disputarse");
        }
        // HU4 - Escenario 3: todos finalizados
        else if (pending.isEmpty()) {
            response.put("message",
                    "Jornada finalizada. Todos los resultados disponibles");
        }
        // HU4 - Escenario 3: ninguno finalizado aun
        else {
            response.put("message",
                    "La jornada aun no ha comenzado. No hay resultados disponibles");
        }

        response.put("finished", finished);
        response.put("pending", pending);
        return response;
    }

    @Override
    public Map<String, Object> getLastRoundResults() {
        // Buscar jornadas que tienen partidos finalizados
        List<String> roundsWithResults = matchRepository
                .findRoundsWithFinishedMatches();

        // HU4 - Escenario 3: no hay resultados disponibles aun
        if (roundsWithResults.isEmpty()) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message",
                    "No hay resultados disponibles aun. El torneo no ha comenzado");
            response.put("finished", new ArrayList<>());
            return response;
        }

        // Tomar la primera (viene ordenada DESC, es la mas reciente)
        String lastRound = roundsWithResults.get(0);
        return getResultsByRound(lastRound);
    }

    @Override
    public Map<String, Object> getStandingsByGroup(String group) {
        List<Match> matches = matchRepository
                .findByMatchGroupOrderByDateTimeAsc(group);

        if (matches.isEmpty()) {
            throw new RuntimeException(
                    "No se encontraron partidos para el grupo: " + group);
        }

        // Mapa temporal: nombre del equipo → stats
        Map<String, int[]> stats = new LinkedHashMap<>();
        // int[]: [played, won, drawn, lost, goalsFor, goalsAgainst]

        // Inicializar todos los equipos del grupo
        for (Match m : matches) {
            if (m.getHomeTeam() != null) {
                stats.putIfAbsent(m.getHomeTeam().getName(), new int[6]);
            }
            if (m.getAwayTeam() != null) {
                stats.putIfAbsent(m.getAwayTeam().getName(), new int[6]);
            }
        }

        // Calcular stats solo de partidos FINALIZADOS
        for (Match m : matches) {
            if (m.getStatus() != MatchStatus.FINISHED) continue;

            int hg = m.getHomeScore() != null ? m.getHomeScore() : 0;
            int ag = m.getAwayScore() != null ? m.getAwayScore() : 0;

            // Calcular stats del equipo LOCAL si existe en la BD
            if (m.getHomeTeam() != null
                    && stats.containsKey(m.getHomeTeam().getName())) {
                String home = m.getHomeTeam().getName();
                stats.get(home)[0]++; // played
                stats.get(home)[4] += hg; // goalsFor
                stats.get(home)[5] += ag; // goalsAgainst
                if (hg > ag)      stats.get(home)[1]++; // win
                else if (hg < ag) stats.get(home)[3]++; // loss
                else              stats.get(home)[2]++; // draw
            }

            // Calcular stats del equipo VISITANTE si existe en la BD
            if (m.getAwayTeam() != null
                    && stats.containsKey(m.getAwayTeam().getName())) {
                String away = m.getAwayTeam().getName();
                stats.get(away)[0]++; // played
                stats.get(away)[4] += ag; // goalsFor
                stats.get(away)[5] += hg; // goalsAgainst
                if (ag > hg)      stats.get(away)[1]++; // win
                else if (ag < hg) stats.get(away)[3]++; // loss
                else              stats.get(away)[2]++; // draw
            }
        }

        // Convertir a DTOs y ordenar por puntos DESC
        List<StandingsDTO> standings = new ArrayList<>();

        for (Match m : matches) {
            // Procesar homeTeam
            if (m.getHomeTeam() != null) {
                String name = m.getHomeTeam().getName();
                if (stats.containsKey(name)) {
                    int[] s = stats.remove(name); // remove evita duplicados
                    int points = s[1] * 3 + s[2];
                    standings.add(StandingsDTO.builder()
                            .team(name)
                            .teamCode(m.getHomeTeam().getFifaCode() != null
                                    ? m.getHomeTeam().getFifaCode() : "")
                            .teamFlag(m.getHomeTeam().getFlagUrl() != null
                                    ? m.getHomeTeam().getFlagUrl() : "")
                            .played(s[0]).won(s[1]).drawn(s[2]).lost(s[3])
                            .goalsFor(s[4]).goalsAgainst(s[5])
                            .goalDifference(s[4] - s[5])
                            .points(points)
                            .build());
                }
            }
            // Procesar awayTeam
            if (m.getAwayTeam() != null) {
                String name = m.getAwayTeam().getName();
                if (stats.containsKey(name)) {
                    int[] s = stats.remove(name);
                    int points = s[1] * 3 + s[2];
                    standings.add(StandingsDTO.builder()
                            .team(name)
                            .teamCode(m.getAwayTeam().getFifaCode() != null
                                    ? m.getAwayTeam().getFifaCode() : "")
                            .teamFlag(m.getAwayTeam().getFlagUrl() != null
                                    ? m.getAwayTeam().getFlagUrl() : "")
                            .played(s[0]).won(s[1]).drawn(s[2]).lost(s[3])
                            .goalsFor(s[4]).goalsAgainst(s[5])
                            .goalDifference(s[4] - s[5])
                            .points(points)
                            .build());
                }
            }
        }

        // Ordenar: primero por puntos, luego por diferencia de goles
        standings.sort((a, b) -> {
            if (b.getPoints() != a.getPoints())
                return b.getPoints() - a.getPoints();
            return b.getGoalDifference() - a.getGoalDifference();
        });

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("group", group);
        response.put("standings", standings);

        long finishedCount = matches.stream()
                .filter(m -> m.getStatus() == MatchStatus.FINISHED)
                .count();

        response.put("matchesPlayed", finishedCount);
        response.put("totalMatches", matches.size());

        if (finishedCount == 0) {
            response.put("message", "El grupo aun no ha disputado partidos");
        } else if (finishedCount < matches.size()) {
            response.put("message", "Grupo en curso. Tabla parcial");
        } else {
            response.put("message", "Grupo finalizado. Tabla definitiva");
        }

        return response;
    }

    // Detecta la fase del torneo según el nombre del round
    private TournamentPhase detectPhase(String roundName) {
        if (roundName == null) return TournamentPhase.GROUP;
        String lower = roundName.toLowerCase();
        if (lower.contains("matchday") || lower.contains("group")) return TournamentPhase.GROUP;
        if (lower.contains("round of 16") || lower.contains("octavos")) return TournamentPhase.ROUND_OF_16;
        if (lower.contains("quarter")) return TournamentPhase.QUARTER_FINAL;
        if (lower.contains("semi")) return TournamentPhase.SEMI_FINAL;
        if (lower.contains("third") || lower.contains("tercer")) return TournamentPhase.THIRD_PLACE;
        if (lower.contains("final")) return TournamentPhase.FINAL;
        return TournamentPhase.GROUP;
    }

    private MatchResponseDTO toDTO(Match m) {
        String result = null;
        if (m.getHomeScore() != null && m.getAwayScore() != null) {
            String home = m.getHomeTeam() != null ? m.getHomeTeam().getName() : "TBD";
            String away = m.getAwayTeam() != null ? m.getAwayTeam().getName() : "TBD";
            result = home + " " + m.getHomeScore() + " - " + m.getAwayScore() + " " + away;
        }

        return MatchResponseDTO.builder()
                .id(m.getId())
                .dateTime(m.getDateTime())
                .roundName(m.getRoundName())
                .phase(m.getPhase())
                .status(m.getStatus())
                .homeTeam(m.getHomeTeam() != null ? m.getHomeTeam().getName() : "TBD")
                .homeTeamCode(m.getHomeTeam() != null ? m.getHomeTeam().getFifaCode() : "")
                .homeTeamFlag(m.getHomeTeam() != null ? m.getHomeTeam().getFlagUrl() : "")
                .awayTeam(m.getAwayTeam() != null ? m.getAwayTeam().getName() : "TBD")
                .awayTeamCode(m.getAwayTeam() != null ? m.getAwayTeam().getFifaCode() : "")
                .awayTeamFlag(m.getAwayTeam() != null ? m.getAwayTeam().getFlagUrl() : "")
                .homeScore(m.getHomeScore())
                .awayScore(m.getAwayScore())
                .matchGroup(m.getMatchGroup())
                .result(result)
                .stadium(m.getStadium() != null ? m.getStadium().getName() : "Por confirmar")
                .refreshed(m.getRefreshed())
                .build();
    }
}

