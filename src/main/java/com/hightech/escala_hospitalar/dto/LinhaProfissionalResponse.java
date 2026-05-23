package com.hightech.escala_hospitalar.dto;

import java.util.List;
import java.util.Map;

public record LinhaProfissionalResponse(
        ProfissionalResponse profissional,
        int horasAlocadas,
        boolean limitAtingido,
        Map<String, List<PlantaoResponse>> plantoesPorDia
) {
}
