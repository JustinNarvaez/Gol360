package com.pjg360.PJG360.repositories;

import com.pjg360.PJG360.model.entities.MatchEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchEventRepository  extends JpaRepository<MatchEvent, Long> {


    List<MatchEvent> findByMatchIdOrderByEventMinuteAsc(Long matchId);


    void deleteByMatchId(Long matchId);


    long countByMatchId(Long matchId);

}
