package com.hightech.escala_hospitalar.dto;


import com.hightech.escala_hospitalar.enuns.Turno;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PlantaoRequest(
        @NotNull(message = "ID do profissional é obrigatório")
        Long profissionalId,

        @NotNull(message = "Data é obrigatória")
        LocalDate data,

        @NotNull(message = "Turno é obrigatório")
        Turno turno
) {
}
