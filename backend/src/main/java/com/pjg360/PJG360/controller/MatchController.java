package com.pjg360.PJG360.controller;

import com.pjg360.PJG360.enums.MatchStatus;
import com.pjg360.PJG360.enums.TournamentPhase;
import com.pjg360.PJG360.model.dtos.MatchResponseDTO;
import com.pjg360.PJG360.services.IMatchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController                          // ← FALTA ESTO
@RequestMapping("/pjg360/api")
public class MatchController {

    private final IMatchService matchService;
    private final RestTemplate restTemplate;


    public MatchController(IMatchService matchService, RestTemplate restTemplate) {
        this.matchService = matchService;
        this.restTemplate = restTemplate;  // ← así, con this.
    }

    @GetMapping("/matches/debug-json")
    public ResponseEntity<String> verJson() {
        try {
            String json = restTemplate.getForObject(
                    "https://raw.githubusercontent.com/openfootball/worldcup.json/master/2026/worldcup.json",
                    String.class);
            return new ResponseEntity<>(json, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/matches/load")
    public ResponseEntity<String> cargarPartidos() {
        System.out.println("Cargando partidos desde API externa...");
        return new ResponseEntity<>(matchService.loadMatchesFromApi(), HttpStatus.OK);
    }

    @GetMapping("/matches")
    public ResponseEntity<List<MatchResponseDTO>> todos() {
        System.out.println("Listando todos los partidos...");
        return new ResponseEntity<>(matchService.getAllMatches(), HttpStatus.OK);
    }

    @GetMapping("/matches/{id}")
    public ResponseEntity<MatchResponseDTO> porId(@PathVariable Long id) {
        System.out.println("Buscando partido con ID: " + id);
        return new ResponseEntity<>(matchService.getById(id), HttpStatus.OK);
    }

    @GetMapping("/matches/phase/{phase}")
    public ResponseEntity<List<MatchResponseDTO>> porFase(@PathVariable TournamentPhase phase) {
        System.out.println("Buscando partidos de fase: " + phase);
        return new ResponseEntity<>(matchService.getMatchesByPhase(phase), HttpStatus.OK);
    }

    @GetMapping("/matches/team/{fifaCode}")
    public ResponseEntity<List<MatchResponseDTO>> porEquipo(@PathVariable String fifaCode) {
        System.out.println("Buscando partidos del equipo: " + fifaCode);
        return new ResponseEntity<>(matchService.getMatchesByTeam(fifaCode.toUpperCase()), HttpStatus.OK);
    }

    @GetMapping("/matches/scheduled")
    public ResponseEntity<List<MatchResponseDTO>> programados() {
        System.out.println("Listando partidos programados...");
        return new ResponseEntity<>(matchService.getScheduledMatches(), HttpStatus.OK);
    }

    @GetMapping("/matches/finished")
    public ResponseEntity<List<MatchResponseDTO>> finalizados() {
        System.out.println("Listando partidos finalizados...");
        return new ResponseEntity<>(matchService.getFinishedMatches(), HttpStatus.OK);
    }

    @GetMapping("/matches/round/{round}")
    public ResponseEntity<List<MatchResponseDTO>> porJornada(@PathVariable String round) {
        System.out.println("Buscando partidos de jornada: " + round);
        return new ResponseEntity<>(matchService.getMatchesByRound(round), HttpStatus.OK);
    }

    @PutMapping("/matches/{matchId}/status")
    public ResponseEntity<?> updateMatchStatus(
            @PathVariable Long matchId,
            @RequestParam MatchStatus status) {
        try {
            MatchResponseDTO updated = matchService.updateMatchStatus(matchId, status);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // HU4 - Resultados de una jornada especifica
    @GetMapping("/matches/results/round/{roundName}")
    public ResponseEntity<?> getResultsByRound(@PathVariable String roundName) {
        try {
            Map<String, Object> results = matchService.getResultsByRound(roundName);
            return new ResponseEntity<>(results, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // HU4 - Resultados de la ultima jornada disputada
    @GetMapping("/matches/results/last-round")
    public ResponseEntity<?> getLastRoundResults() {
        try {
            Map<String, Object> results = matchService.getLastRoundResults();
            return new ResponseEntity<>(results, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
