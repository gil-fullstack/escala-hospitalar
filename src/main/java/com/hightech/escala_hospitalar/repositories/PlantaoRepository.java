package com.hightech.escala_hospitalar.repositories;


import com.hightech.escala_hospitalar.entities.Plantao;
import com.hightech.escala_hospitalar.enuns.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PlantaoRepository extends JpaRepository<Plantao, Long> {

    boolean existsByProfissionalIdAndDataAndTurno(Long profissionalId, LocalDate data, Turno turno);

    List<Plantao> findByProfissionalIdAndDataBetween(Long profissionalId, LocalDate inicio, LocalDate fim);

    @Query("SELECT p FROM Plantao p JOIN FETCH p.profissional WHERE p.data BETWEEN :inicio AND :fim ORDER BY p.profissional.nome ASC, p.data ASC")
    List<Plantao> findByDataBetweenComProfissional(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}
