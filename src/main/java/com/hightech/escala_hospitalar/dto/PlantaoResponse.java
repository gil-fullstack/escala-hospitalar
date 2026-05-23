package com.hightech.escala_hospitalar.dto;

import com.hightech.escala_hospitalar.domain.Plantao;
import com.hightech.escala_hospitalar.domain.Turno;
import java.time.LocalDate;

public record PlantaoResponse(
        Long id,
        ProfissionalResponse profissional,
        LocalDate data,
        Turno turno,
        String horaInicio,
        String horaFim
) {

    public static PlantaoResponse from(Plantao p) {
        return new PlantaoResponse(
                p.getId(),
                ProfissionalResponse.from(p.getProfissional()),
                p.getData(),
                p.getTurno(),
                p.getTurno().getHoraInicio(),
                p.getTurno().getHoraFim()
        );
    }
}
