package com.pjg360.PJG360.services;

import com.pjg360.PJG360.enums.MatchStatus;
import com.pjg360.PJG360.enums.TournamentPhase;
import com.pjg360.PJG360.model.dtos.MatchResponseDTO;

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
    MatchResponseDTO updateMatchStatus(Long matchId, MatchStatus newStatus);
    Map<String, Object> getResultsByRound(String roundName);
    Map<String, Object> getLastRoundResults();

}
