package com.pjg360.PJG360.controller;

import com.pjg360.PJG360.model.dtos.MatchEventRequestDTO;
import com.pjg360.PJG360.model.dtos.MatchEventResponseDTO;
import com.pjg360.PJG360.services.IMatchEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pjg360/api")
public class MatchEventController {

    private final IMatchEventService matchEventService;

    public MatchEventController(IMatchEventService matchEventService) {
        this.matchEventService = matchEventService;
    }

    // HU2 - Listar eventos de un partido en orden cronologico
    @GetMapping("/matches/{matchId}/events")
    public ResponseEntity<?> getEventsByMatch(@PathVariable Long matchId) {
        try {
            List<MatchEventResponseDTO> events =
                    matchEventService.getEventsByMatchId(matchId);
            return new ResponseEntity<>(events, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/matches/{matchId}/events")
    public ResponseEntity<?> createEvent(
            @PathVariable Long matchId,
            @RequestBody MatchEventRequestDTO request) {
        try {
            MatchEventResponseDTO created =
                    matchEventService.createEvent(matchId, request);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/matches/{matchId}/events/{eventId}")
    public ResponseEntity<?> updateEvent(
            @PathVariable Long matchId,
            @PathVariable Long eventId,
            @RequestBody MatchEventRequestDTO request) {
        try {
            MatchEventResponseDTO updated =
                    matchEventService.updateEvent(matchId, eventId, request);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/matches/{matchId}/events/{eventId}")
    public ResponseEntity<?> deleteEvent(
            @PathVariable Long matchId,
            @PathVariable Long eventId) {
        try {
            matchEventService.deleteEvent(matchId, eventId);
            return new ResponseEntity<>("Evento eliminado exitosamente", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}