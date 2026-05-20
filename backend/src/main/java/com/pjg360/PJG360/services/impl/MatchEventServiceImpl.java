package com.pjg360.PJG360.services.impl;

import com.pjg360.PJG360.enums.MatchStatus;
import com.pjg360.PJG360.model.dtos.MatchEventRequestDTO;
import com.pjg360.PJG360.model.dtos.MatchEventResponseDTO;
import com.pjg360.PJG360.model.entities.Match;
import com.pjg360.PJG360.model.entities.MatchEvent;
import com.pjg360.PJG360.model.entities.Player;
import com.pjg360.PJG360.repositories.MatchEventRepository;
import com.pjg360.PJG360.repositories.MatchRepository;
import com.pjg360.PJG360.repositories.PlayerRepository;
import com.pjg360.PJG360.services.IMatchEventService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchEventServiceImpl implements IMatchEventService {

    private final MatchEventRepository matchEventRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;

    public MatchEventServiceImpl(MatchEventRepository matchEventRepository,
                                 MatchRepository matchRepository,
                                 PlayerRepository playerRepository) {
        this.matchEventRepository = matchEventRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    public List<MatchEventResponseDTO> getEventsByMatchId(Long matchId) {
        if (!matchRepository.existsById(matchId)) {
            throw new RuntimeException("Partido con id " + matchId + " no encontrado");
        }

        return matchEventRepository
                .findByMatchIdOrderByEventMinuteAsc(matchId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MatchEventResponseDTO createEvent(Long matchId, MatchEventRequestDTO request) {
        // 1. Validar que el partido existe
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException(
                        "Partido con id " + matchId + " no encontrado"));

        // 2. Validar estado del partido (no se pueden crear eventos en partidos no iniciados o finalizados)
        if (match.getStatus() == MatchStatus.SCHEDULED) {
            throw new RuntimeException(
                    "No se pueden registrar eventos en un partido que aun no ha iniciado");
        }
        if (match.getStatus() == MatchStatus.FINISHED) {
            throw new RuntimeException(
                    "No se pueden registrar eventos en un partido finalizado");
        }

        // 3. Validar campos obligatorios
        if (request.getEvent() == null) {
            throw new RuntimeException("El tipo de evento es obligatorio");
        }
        if (request.getEventMinute() == null || request.getEventMinute() < 1) {
            throw new RuntimeException("El minuto del evento debe ser mayor a 0");
        }

        // 4. Buscar jugador si viene playerId
        Player player = null;
        if (request.getPlayerId() != null) {
            player = playerRepository.findById(request.getPlayerId())
                    .orElseThrow(() -> new RuntimeException(
                            "Jugador con id " + request.getPlayerId() + " no encontrado"));
        }

        // 5. Crear y guardar el evento
        MatchEvent newEvent = new MatchEvent();
        newEvent.setEvent(request.getEvent());
        newEvent.setEventMinute(request.getEventMinute());
        newEvent.setPlayer(player);
        newEvent.setMatch(match);

        MatchEvent saved = matchEventRepository.save(newEvent);

        // 6. Marcar partido como refreshed = true (acabamos de actualizar)
        match.setRefreshed(true);
        matchRepository.save(match);

        return toResponseDTO(saved);
    }

    @Override
    public MatchEventResponseDTO updateEvent(Long matchId, Long eventId,
                                             MatchEventRequestDTO request) {
        // 1. Validar partido
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException(
                        "Partido con id " + matchId + " no encontrado"));

        // No se pueden editar eventos si el partido no ha iniciado
        if (match.getStatus() == MatchStatus.SCHEDULED) {
            throw new RuntimeException(
                    "No se pueden editar eventos de un partido que aun no ha iniciado");
        }

        // 2. No se pueden editar eventos en partidos finalizados
        if (match.getStatus() == MatchStatus.FINISHED) {
            throw new RuntimeException(
                    "No se pueden editar eventos de un partido finalizado");
        }

        // 3. Validar que el evento existe y pertenece al partido
        MatchEvent existing = matchEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException(
                        "Evento con id " + eventId + " no encontrado"));

        if (!existing.getMatch().getId().equals(matchId)) {
            throw new RuntimeException(
                    "El evento " + eventId + " no pertenece al partido " + matchId);
        }

        // 4. Actualizar campos
        if (request.getEvent() != null) {
            existing.setEvent(request.getEvent());
        }
        if (request.getEventMinute() != null) {
            if (request.getEventMinute() < 1) {
                throw new RuntimeException("El minuto del evento debe ser mayor a 0");
            }
            existing.setEventMinute(request.getEventMinute());
        }
        if (request.getPlayerId() != null) {
            Player player = playerRepository.findById(request.getPlayerId())
                    .orElseThrow(() -> new RuntimeException(
                            "Jugador con id " + request.getPlayerId() + " no encontrado"));
            existing.setPlayer(player);
        }

        MatchEvent saved = matchEventRepository.save(existing);

        // 5. Marcar partido como refreshed
        match.setRefreshed(true);
        matchRepository.save(match);

        return toResponseDTO(saved);
    }

    @Override
    public void deleteEvent(Long matchId, Long eventId) {
        // 1. Validar partido
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException(
                        "Partido con id " + matchId + " no encontrado"));


        // 2. No se pueden eliminar eventos si el partido no ha iniciado
        if (match.getStatus() == MatchStatus.SCHEDULED) {
            throw new RuntimeException(
                    "No se pueden eliminar eventos de un partido que aun no ha iniciado");
        }

        // 2. No se pueden eliminar eventos de partidos finalizados
        if (match.getStatus() == MatchStatus.FINISHED) {
            throw new RuntimeException(
                    "No se pueden eliminar eventos de un partido finalizado");
        }

        // 3. Validar que el evento existe y pertenece al partido
        MatchEvent existing = matchEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException(
                        "Evento con id " + eventId + " no encontrado"));

        if (!existing.getMatch().getId().equals(matchId)) {
            throw new RuntimeException(
                    "El evento " + eventId + " no pertenece al partido " + matchId);
        }

        // 4. Eliminar
        matchEventRepository.delete(existing);

        // 5. Marcar partido como refreshed
        match.setRefreshed(true);
        matchRepository.save(match);
    }

    private MatchEventResponseDTO toResponseDTO(MatchEvent event) {
        return MatchEventResponseDTO.builder()
                .id(event.getId())
                .event(event.getEvent())
                .eventMinute(event.getEventMinute())
                .playerId(event.getPlayer() != null ? event.getPlayer().getId() : null)
                .playerName(event.getPlayer() != null ? event.getPlayer().getFullName() : null)
                .playerPosition(event.getPlayer() != null ? event.getPlayer().getPosition() : null)
                .matchId(event.getMatch().getId())
                .build();
    }
}