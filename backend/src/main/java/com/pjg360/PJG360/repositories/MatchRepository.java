package com.pjg360.PJG360.repositories;

import com.pjg360.PJG360.enums.MatchStatus;
import com.pjg360.PJG360.enums.TournamentPhase;
import com.pjg360.PJG360.model.entities.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByPhase(TournamentPhase phase);
    List<Match> findByStatus(MatchStatus status);
    List<Match> findByHomeTeamFifaCodeOrAwayTeamFifaCode(String home, String away);
    List<Match> findByRoundName(String roundName);
    // Todos los partidos de una jornada específica
    List<Match> findByRoundNameOrderByDateTimeAsc(String roundName);
    // Partidos de un grupo especifico
    List<Match> findByMatchGroupOrderByDateTimeAsc(String matchGroup);
    // Jornadas que tienen al menos un partido FINISHED
    @Query("SELECT DISTINCT m.roundName FROM Match m WHERE m.status = 'FINISHED' ORDER BY m.roundName DESC")
    List<String> findRoundsWithFinishedMatches();
}
