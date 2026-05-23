package com.hightech.escala_hospitalar.dto;

import com.hightech.escala_hospitalar.domain.Categoria;
import com.hightech.escala_hospitalar.domain.Profissional;

public record ProfissionalResponse(
        Long id,
        String nome,
        String crm,
        Categoria categoria,
        Integer cargaHorariaSemanal
) {

    public static ProfissionalResponse from(Profissional p) {
        return new ProfissionalResponse(p.getId(), p.getNome(), p.getCrm(),
                p.getCategoria(), p.getCargaHorariaSemanal());
    }
}
