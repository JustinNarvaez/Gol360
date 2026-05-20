package com.pjg360.PJG360.controller;

import com.pjg360.PJG360.model.dtos.*;
import com.pjg360.PJG360.services.IPollaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pjg360/api")
public class PollaController {

    private final IPollaService pollaService;

    public PollaController(IPollaService pollaService) {
        this.pollaService = pollaService;
    }

    // Crear una polla nueva
    @PostMapping("/pollas")
    public ResponseEntity<?> createPolla(@RequestBody PollaRequestDTO request) {
        try {
            PollaResponseDTO created = pollaService.createPolla(request);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Listar todas las pollas
    @GetMapping("/pollas")
    public ResponseEntity<List<PollaResponseDTO>> getAllPollas() {
        return new ResponseEntity<>(pollaService.getAllPollas(), HttpStatus.OK);
    }

    // Detalle de una polla
    @GetMapping("/pollas/{pollaId}")
    public ResponseEntity<?> getPollaById(@PathVariable Long pollaId) {
        try {
            return new ResponseEntity<>(
                    pollaService.getPollaById(pollaId), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Unirse a una polla con el codigo
    @PostMapping("/pollas/join/{pollaCode}/user/{userId}")
    public ResponseEntity<?> joinPolla(
            @PathVariable String pollaCode,
            @PathVariable Long userId) {
        try {
            return new ResponseEntity<>(
                    pollaService.joinPolla(pollaCode, userId), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Hacer una prediccion
    @PostMapping("/pollas/{pollaId}/forecasts")
    public ResponseEntity<?> createForecast(
            @PathVariable Long pollaId,
            @RequestBody ForecastRequestDTO request) {
        try {
            return new ResponseEntity<>(
                    pollaService.createForecast(pollaId, request), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Ver predicciones de un usuario en una polla
    @GetMapping("/pollas/{pollaId}/forecasts/user/{userId}")
    public ResponseEntity<?> getForecastsByUser(
            @PathVariable Long pollaId,
            @PathVariable Long userId) {
        try {
            return new ResponseEntity<>(
                    pollaService.getForecastsByUser(pollaId, userId), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Ver ranking de una polla
    @GetMapping("/pollas/{pollaId}/rankings")
    public ResponseEntity<?> getRanking(@PathVariable Long pollaId) {
        try {
            return new ResponseEntity<>(
                    pollaService.getRankingByPolla(pollaId), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Calcular puntos de la polla
    @PostMapping("/pollas/{pollaId}/calculate")
    public ResponseEntity<?> calculatePoints(@PathVariable Long pollaId) {
        try {
            Map<String, Object> result = pollaService.calculatePoints(pollaId);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Cerrar polla
    @PutMapping("/pollas/{pollaId}/close")
    public ResponseEntity<?> closePolla(@PathVariable Long pollaId) {
        try {
            return new ResponseEntity<>(
                    pollaService.closePolla(pollaId), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}