package com.pjg360.PJG360;

import com.pjg360.PJG360.enums.EventType;
import com.pjg360.PJG360.enums.MatchStatus;
import com.pjg360.PJG360.model.dtos.MatchEventRequestDTO;
import com.pjg360.PJG360.model.dtos.MatchEventResponseDTO;
import com.pjg360.PJG360.model.entities.Match;
import com.pjg360.PJG360.model.entities.MatchEvent;
import com.pjg360.PJG360.model.entities.Player;
import com.pjg360.PJG360.repositories.MatchEventRepository;
import com.pjg360.PJG360.repositories.MatchRepository;
import com.pjg360.PJG360.repositories.PlayerRepository;
import com.pjg360.PJG360.services.impl.MatchEventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchEventServiceImplTest {

    @Mock
    private MatchEventRepository matchEventRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private MatchEventServiceImpl matchEventService;

    private Match matchEnCurso;
    private Match matchFinalizado;
    private Match matchProgramado;
    private MatchEvent evento;
    private Player jugador;

    @BeforeEach
    void setUp() {
        matchEnCurso = Match.builder()
                .id(1L)
                .status(MatchStatus.IN_PROGRESS)
                .refreshed(false)
                .build();

        matchFinalizado = Match.builder()
                .id(2L)
                .status(MatchStatus.FINISHED)
                .build();

        matchProgramado = Match.builder()
                .id(3L)
                .status(MatchStatus.SCHEDULED)
                .build();

        jugador = new Player();
        jugador.setId(10L);
        jugador.setFullName("Cristiano Ronaldo");

        evento = MatchEvent.builder()
                .id(100L)
                .event(EventType.GOAL)
                .eventMinute(45)
                .player(jugador)
                .match(matchEnCurso)
                .build();
    }

    // Obtener eventos de partido por ID

    @Test
    void getEventsByMatchId_exitoso() {
        when(matchRepository.existsById(1L)).thenReturn(true);
        when(matchEventRepository.findByMatchIdOrderByEventMinuteAsc(1L))
                .thenReturn(List.of(evento));

        List<MatchEventResponseDTO> resultado = matchEventService.getEventsByMatchId(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(EventType.GOAL, resultado.get(0).getEvent());
    }

    @Test
    void getEventsByMatchId_partidoNoExiste_lanzaExcepcion() {
        when(matchRepository.existsById(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> matchEventService.getEventsByMatchId(99L));

        assertEquals("Partido con id 99 no encontrado", ex.getMessage());
    }

    @Test
    void getEventsByMatchId_sinEventos_retornaListaVacia() {
        when(matchRepository.existsById(1L)).thenReturn(true);
        when(matchEventRepository.findByMatchIdOrderByEventMinuteAsc(1L))
                .thenReturn(List.of());

        List<MatchEventResponseDTO> resultado = matchEventService.getEventsByMatchId(1L);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    // Crear evento

    @Test
    void createEvent_exitoso_sinJugador() {
        MatchEventRequestDTO request = new MatchEventRequestDTO(EventType.GOAL, 30, null);

        MatchEvent eventoGuardado = MatchEvent.builder()
                .id(1L)
                .event(EventType.GOAL)
                .eventMinute(30)
                .player(null)
                .match(matchEnCurso)
                .build();

        when(matchRepository.findById(1L)).thenReturn(Optional.of(matchEnCurso));
        when(matchEventRepository.save(any())).thenReturn(eventoGuardado);

        MatchEventResponseDTO resultado = matchEventService.createEvent(1L, request);

        assertNotNull(resultado);
        assertEquals(EventType.GOAL, resultado.getEvent());
        assertEquals(30, resultado.getEventMinute());
        verify(matchRepository).save(matchEnCurso);
    }

    @Test
    void createEvent_exitoso_conJugador() {
        MatchEventRequestDTO request = new MatchEventRequestDTO(EventType.GOAL, 45, 10L);

        MatchEvent eventoGuardado = MatchEvent.builder()
                .id(1L)
                .event(EventType.GOAL)
                .eventMinute(45)
                .player(jugador)
                .match(matchEnCurso)
                .build();

        when(matchRepository.findById(1L)).thenReturn(Optional.of(matchEnCurso));
        when(playerRepository.findById(10L)).thenReturn(Optional.of(jugador));
        when(matchEventRepository.save(any())).thenReturn(eventoGuardado);

        MatchEventResponseDTO resultado = matchEventService.createEvent(1L, request);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getPlayerId());
    }

    @Test
    void createEvent_partidoNoExiste_lanzaExcepcion() {
        MatchEventRequestDTO request = new MatchEventRequestDTO(EventType.GOAL, 30, null);

        when(matchRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> matchEventService.createEvent(99L, request));

        assertEquals("Partido con id 99 no encontrado", ex.getMessage());
    }

    @Test
    void createEvent_partidoProgramado_lanzaExcepcion() {
        MatchEventRequestDTO request = new MatchEventRequestDTO(EventType.GOAL, 30, null);

        when(matchRepository.findById(3L)).thenReturn(Optional.of(matchProgramado));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> matchEventService.createEvent(3L, request));

        assertEquals("No se pueden registrar eventos en un partido que aun no ha iniciado", ex.getMessage());
    }

    @Test
    void createEvent_partidoFinalizado_lanzaExcepcion() {
        MatchEventRequestDTO request = new MatchEventRequestDTO(EventType.GOAL, 30, null);

        when(matchRepository.findById(2L)).thenReturn(Optional.of(matchFinalizado));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> matchEventService.createEvent(2L, request));

        assertEquals("No se pueden registrar eventos en un partido finalizado", ex.getMessage());
    }

    @Test
    void createEvent_sinTipoEvento_lanzaExcepcion() {
        MatchEventRequestDTO request = new MatchEventRequestDTO(null, 30, null);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(matchEnCurso));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> matchEventService.createEvent(1L, request));

        assertEquals("El tipo de evento es obligatorio", ex.getMessage());
    }

    @Test
    void createEvent_minutoInvalido_lanzaExcepcion() {
        MatchEventRequestDTO request = new MatchEventRequestDTO(EventType.GOAL, 0, null);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(matchEnCurso));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> matchEventService.createEvent(1L, request));

        assertEquals("El minuto del evento debe ser mayor a 0", ex.getMessage());
    }

    @Test
    void createEvent_jugadorNoExiste_lanzaExcepcion() {
        MatchEventRequestDTO request = new MatchEventRequestDTO(EventType.GOAL, 30, 99L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(matchEnCurso));
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> matchEventService.createEvent(1L, request));

        assertEquals("Jugador con id 99 no encontrado", ex.getMessage());
    }

    // Actualizar evento

    @Test
    void updateEvent_exitoso() {
        MatchEventRequestDTO request = new MatchEventRequestDTO(EventType.YELLOW_CARD, 60, null);

        MatchEvent eventoActualizado = MatchEvent.builder()
                .id(100L)
                .event(EventType.YELLOW_CARD)
                .eventMinute(60)
                .player(jugador)
                .match(matchEnCurso)
                .build();

        when(matchRepository.findById(1L)).thenReturn(Optional.of(matchEnCurso));
        when(matchEventRepository.findById(100L)).thenReturn(Optional.of(evento));
        when(matchEventRepository.save(any())).thenReturn(eventoActualizado);

        MatchEventResponseDTO resultado = matchEventService.updateEvent(1L, 100L, request);

        assertNotNull(resultado);
        assertEquals(EventType.YELLOW_CARD, resultado.getEvent());
    }

    @Test
    void updateEvent_partidoNoExiste_lanzaExcepcion() {
        MatchEventRequestDTO request = new MatchEventRequestDTO(EventType.GOAL, 30, null);

        when(matchRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> matchEventService.updateEvent(99L, 100L, request));

        assertEquals("Partido con id 99 no encontrado", ex.getMessage());
    }

    @Test
    void updateEvent_partidoProgramado_lanzaExcepcion() {
        MatchEventRequestDTO request = new MatchEventRequestDTO(EventType.GOAL, 30, null);

        when(matchRepository.findById(3L)).thenReturn(Optional.of(matchProgramado));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> matchEventService.updateEvent(3L, 100L, request));

        assertEquals("No se pueden editar eventos de un partido que aun no ha iniciado", ex.getMessage());
    }

    @Test
    void updateEvent_partidoFinalizado_lanzaExcepcion() {
        MatchEventRequestDTO request = new MatchEventRequestDTO(EventType.GOAL, 30, null);

        when(matchRepository.findById(2L)).thenReturn(Optional.of(matchFinalizado));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> matchEventService.updateEvent(2L, 100L, request));

        assertEquals("No se pueden editar eventos de un partido finalizado", ex.getMessage());
    }

    @Test
    void updateEvent_eventoNoPerteneceAlPartido_lanzaExcepcion() {
        MatchEvent eventoDeOtroPartido = MatchEvent.builder()
                .id(100L)
                .match(Match.builder().id(99L).build())
                .build();

        MatchEventRequestDTO request = new MatchEventRequestDTO(EventType.GOAL, 30, null);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(matchEnCurso));
        when(matchEventRepository.findById(100L)).thenReturn(Optional.of(eventoDeOtroPartido));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> matchEventService.updateEvent(1L, 100L, request));

        assertEquals("El evento 100 no pertenece al partido 1", ex.getMessage());
    }

    // Eliminar evento

    @Test
    void deleteEvent_exitoso() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(matchEnCurso));
        when(matchEventRepository.findById(100L)).thenReturn(Optional.of(evento));

        assertDoesNotThrow(() -> matchEventService.deleteEvent(1L, 100L));

        verify(matchEventRepository).delete(evento);
        verify(matchRepository).save(matchEnCurso);
    }

    @Test
    void deleteEvent_partidoNoExiste_lanzaExcepcion() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> matchEventService.deleteEvent(99L, 100L));

        assertEquals("Partido con id 99 no encontrado", ex.getMessage());
        verify(matchEventRepository, never()).delete(any());
    }

    @Test
    void deleteEvent_partidoProgramado_lanzaExcepcion() {
        when(matchRepository.findById(3L)).thenReturn(Optional.of(matchProgramado));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> matchEventService.deleteEvent(3L, 100L));

        assertEquals("No se pueden eliminar eventos de un partido que aun no ha iniciado", ex.getMessage());
        verify(matchEventRepository, never()).delete(any());
    }

    @Test
    void deleteEvent_partidoFinalizado_lanzaExcepcion() {
        when(matchRepository.findById(2L)).thenReturn(Optional.of(matchFinalizado));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> matchEventService.deleteEvent(2L, 100L));

        assertEquals("No se pueden eliminar eventos de un partido finalizado", ex.getMessage());
        verify(matchEventRepository, never()).delete(any());
    }

    @Test
    void deleteEvent_eventoNoPerteneceAlPartido_lanzaExcepcion() {
        MatchEvent eventoDeOtroPartido = MatchEvent.builder()
                .id(100L)
                .match(Match.builder().id(99L).build())
                .build();

        when(matchRepository.findById(1L)).thenReturn(Optional.of(matchEnCurso));
        when(matchEventRepository.findById(100L)).thenReturn(Optional.of(eventoDeOtroPartido));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> matchEventService.deleteEvent(1L, 100L));

        assertEquals("El evento 100 no pertenece al partido 1", ex.getMessage());
        verify(matchEventRepository, never()).delete(any());
    }
}
