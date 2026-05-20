package com.pjg360.PJG360.services;

import com.pjg360.PJG360.model.dtos.MatchEventRequestDTO;
import com.pjg360.PJG360.model.dtos.MatchEventResponseDTO;

import java.util.List;

public interface IMatchEventService {
    List<MatchEventResponseDTO> getEventsByMatchId(Long matchId);
    MatchEventResponseDTO createEvent(Long matchId, MatchEventRequestDTO request);
    MatchEventResponseDTO updateEvent(Long matchId, Long eventId, MatchEventRequestDTO request);

    void deleteEvent(Long matchId, Long eventId);

}
