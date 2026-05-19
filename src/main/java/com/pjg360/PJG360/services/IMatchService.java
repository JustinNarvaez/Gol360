package com.pjg360.PJG360.services;

import com.pjg360.PJG360.enums.TournamentPhase;
import com.pjg360.PJG360.model.dtos.MatchResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IMatchService {
    String loadMatchesFromApi();
    List<MatchResponseDTO> getAllMatches();
    List<MatchResponseDTO> getMatchesByPhase(TournamentPhase phase);
    List<MatchResponseDTO> getMatchesByTeam(String fifaCode);
    List<MatchResponseDTO> getMatchesByRound(String roundName);
    List<MatchResponseDTO> getScheduledMatches();
    List<MatchResponseDTO> getFinishedMatches();
    MatchResponseDTO getById(Long id);
    Map<String, Object> getMatchesByDateRange(LocalDate from, LocalDate to);
}
