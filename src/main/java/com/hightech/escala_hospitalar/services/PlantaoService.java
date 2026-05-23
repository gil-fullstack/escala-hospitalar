package com.hightech.escala_hospitalar.services;


import com.hightech.escala_hospitalar.dto.*;
import com.hightech.escala_hospitalar.entities.Plantao;
import com.hightech.escala_hospitalar.entities.Profissional;
import com.hightech.escala_hospitalar.exceptions.RegraDeNegocioException;
import com.hightech.escala_hospitalar.exceptions.ResourceNotFoundException;
import com.hightech.escala_hospitalar.repositories.PlantaoRepository;
import com.hightech.escala_hospitalar.repositories.ProfissionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlantaoService {

    private final PlantaoRepository plantaoRepository;
    private final ProfissionalRepository profissionalRepository;

    public PlantaoService(PlantaoRepository plantaoRepository,
                          ProfissionalRepository profissionalRepository) {
        this.plantaoRepository = plantaoRepository;
        this.profissionalRepository = profissionalRepository;
    }

    @Transactional
    public PlantaoResponse criar(PlantaoRequest request) {
        Profissional profissional = profissionalRepository.findById(request.profissionalId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profissional não encontrado com id: " + request.profissionalId()));

        validarDuplicidade(profissional.getId(), request.data(), request);
        validarCargaHoraria(profissional, request);

        var plantao = new Plantao(profissional, request.data(), request.turno());
        return PlantaoResponse.from(plantaoRepository.save(plantao));
    }

    @Transactional
    public void deletar(Long id) {
        if (!plantaoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Plantão não encontrado com id: " + id);
        }
        plantaoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public EscalaSemanalResponse buscarEscalaSemanal(LocalDate dataInicio) {
        LocalDate dataFim = dataInicio.plusDays(6);

        List<Profissional> profissionais = profissionalRepository.findAllByOrderByNomeAsc();
        List<Plantao> plantoesDaSemana = plantaoRepository
                .findByDataBetweenComProfissional(dataInicio, dataFim);

        Map<Long, List<Plantao>> plantoesPorProfissional = plantoesDaSemana.stream()
                .collect(Collectors.groupingBy(p -> p.getProfissional().getId()));

        List<LinhaProfissionalResponse> linhas = profissionais.stream()
                .map(prof -> construirLinha(prof, plantoesPorProfissional
                        .getOrDefault(prof.getId(), List.of())))
                .toList();

        List<String> dias = dataInicio.datesUntil(dataFim.plusDays(1))
                .map(LocalDate::toString)
                .toList();

        return new EscalaSemanalResponse(
                dataInicio.toString(),
                dataFim.toString(),
                dias,
                linhas
        );
    }

    // --- validações de regra de negócio ---

    private void validarDuplicidade(Long profissionalId, LocalDate data, PlantaoRequest request) {
        if (plantaoRepository.existsByProfissionalIdAndDataAndTurno(
                profissionalId, data, request.turno())) {
            throw new RegraDeNegocioException("Profissional já possui plantão em " + data + " no turno " + request.turno());

        }
    }

    private void validarCargaHoraria(Profissional profissional, PlantaoRequest request) {
        LocalDate inicioSemana = request.data().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate fimSemana = request.data().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        int horasJaAlocadas = plantaoRepository
                .findByProfissionalIdAndDataBetween(profissional.getId(), inicioSemana, fimSemana)
                .stream()
                .mapToInt(p -> p.getTurno().getDuracaoHoras())
                .sum();

        int horasDoNovoPlantao = request.turno().getDuracaoHoras();
        int totalAposInsercao = horasJaAlocadas + horasDoNovoPlantao;

        if (totalAposInsercao > profissional.getCargaHorariaSemanal()) {
            throw new RegraDeNegocioException(String.format(
                    "Alocação excede a carga horária semanal contratada. " +
                    "Já alocado: %dh | Novo plantão: %dh | Total: %dh | Contratado: %dh",
                    horasJaAlocadas, horasDoNovoPlantao,
                    totalAposInsercao, profissional.getCargaHorariaSemanal()));
        }
    }

    // --- mapeamento para resposta semanal ---

    private LinhaProfissionalResponse construirLinha(Profissional profissional, List<Plantao> plantoes) {
        int horasAlocadas = plantoes.stream()
                .mapToInt(p -> p.getTurno().getDuracaoHoras())
                .sum();

        boolean limitAtingido = horasAlocadas >= profissional.getCargaHorariaSemanal();

        Map<String, List<PlantaoResponse>> plantoesPorDia = plantoes.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getData().toString(),
                        Collectors.mapping(PlantaoResponse::from, Collectors.toList())
                ));

        return new LinhaProfissionalResponse(
                ProfissionalResponse.from(profissional),
                horasAlocadas,
                limitAtingido,
                plantoesPorDia
        );
    }
}
