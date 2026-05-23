package com.hightech.escala_hospitalar.controller;

import com.hightech.escala_hospitalar.dto.EscalaSemanalResponse;
import com.hightech.escala_hospitalar.dto.PlantaoRequest;
import com.hightech.escala_hospitalar.dto.PlantaoResponse;

import com.hightech.escala_hospitalar.services.PlantaoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/plantoes")
public class PlantaoController {

    private final PlantaoService service;

    public PlantaoController(PlantaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PlantaoResponse> criar(@Valid @RequestBody PlantaoRequest request) {
        PlantaoResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/semana")
    public ResponseEntity<EscalaSemanalResponse> escalaSemanal(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio) {
        return ResponseEntity.ok(service.buscarEscalaSemanal(dataInicio));
    }
}
