package com.hightech.escala_hospitalar.controller;


import com.hightech.escala_hospitalar.dto.ProfissionalRequest;
import com.hightech.escala_hospitalar.dto.ProfissionalResponse;

import com.hightech.escala_hospitalar.enuns.Categoria;
import com.hightech.escala_hospitalar.services.ProfissionalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/profissionais")
public class ProfissionalController {

    private final ProfissionalService service;

    public ProfissionalController(ProfissionalService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ProfissionalResponse>> listar(
            @RequestParam(required = false) Categoria categoria) {
        return ResponseEntity.ok(service.listar(categoria));
    }

    @PostMapping
    public ResponseEntity<ProfissionalResponse> criar(@Valid @RequestBody ProfissionalRequest request) {
        ProfissionalResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }
}
