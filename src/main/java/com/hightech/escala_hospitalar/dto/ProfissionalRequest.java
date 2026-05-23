package com.hightech.escala_hospitalar.dto;

import com.hightech.escala_hospitalar.domain.Categoria;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfissionalRequest(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "CRM/COREN é obrigatório")
        String crm,

        @NotNull(message = "Categoria é obrigatória")
        Categoria categoria,

        @NotNull(message = "Carga horária semanal é obrigatória")
        @Min(value = 1, message = "Carga horária mínima é 1h")
        @Max(value = 168, message = "Carga horária máxima é 168h")
        Integer cargaHorariaSemanal
) {
}
