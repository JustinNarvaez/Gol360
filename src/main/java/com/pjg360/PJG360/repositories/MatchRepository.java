package com.pjg360.PJG360.repositories;

import com.pjg360.PJG360.enums.MatchStatus;
import com.pjg360.PJG360.enums.TournamentPhase;
import com.pjg360.PJG360.model.entities.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByPhase(TournamentPhase phase);
    List<Match> findByStatus(MatchStatus status);
    List<Match> findByHomeTeamFifaCodeOrAwayTeamFifaCode(String home, String away);
    List<Match> findByRoundName(String roundName);
    @Query("SELECT m FROM Match m WHERE m.dateTime BETWEEN :from AND :to ORDER BY m.dateTime ASC")
    List<Match> findByDateTimeBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
