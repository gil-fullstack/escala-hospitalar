package com.hightech.escala_hospitalar.repositories;

import com.hightech.escala_hospitalar.domain.Categoria;
import com.hightech.escala_hospitalar.domain.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    List<Profissional> findByCategoriaOrderByNomeAsc(Categoria categoria);

    List<Profissional> findAllByOrderByNomeAsc();

    boolean existsByCrm(String crm);
}
