package com.hightech.escala_hospitalar.service;

import com.hightech.escala_hospitalar.domain.Categoria;
import com.hightech.escala_hospitalar.domain.Profissional;
import com.hightech.escala_hospitalar.dto.ProfissionalRequest;
import com.hightech.escala_hospitalar.dto.ProfissionalResponse;
import com.hightech.escala_hospitalar.exception.RegraDeNegocioException;
import com.hightech.escala_hospitalar.exception.ResourceNotFoundException;
import com.hightech.escala_hospitalar.repository.ProfissionalRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfissionalService {

    private final ProfissionalRepository repository;

    public ProfissionalService(ProfissionalRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ProfissionalResponse> listar(Categoria categoria) {
        List<Profissional> profissionais = categoria != null
                ? repository.findByCategoriaOrderByNomeAsc(categoria)
                : repository.findAllByOrderByNomeAsc();

        return profissionais.stream()
                .map(ProfissionalResponse::from)
                .toList();
    }

    @Transactional
    public ProfissionalResponse criar(ProfissionalRequest request) {
        if (repository.existsByCrm(request.crm())) {
            throw new RegraDeNegocioException(
                    "Já existe um profissional cadastrado com o CRM/COREN: " + request.crm());
        }

        var profissional = new Profissional(
                request.nome(),
                request.crm(),
                request.categoria(),
                request.cargaHorariaSemanal()
        );

        return ProfissionalResponse.from(repository.save(profissional));
    }

    @Transactional(readOnly = true)
    public Profissional buscarOuLancar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profissional não encontrado com id: " + id));
    }
}
