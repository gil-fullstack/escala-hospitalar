package com.hightech.escala_hospitalar.dto;

import java.util.List;

public record EscalaSemanalResponse(
        String dataInicio,
        String dataFim,
        List<String> dias,
        List<LinhaProfissionalResponse> linhas
) {
}
