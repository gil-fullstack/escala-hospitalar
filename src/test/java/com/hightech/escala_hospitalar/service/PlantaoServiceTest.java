package com.hightech.escala_hospitalar.service;


import com.hightech.escala_hospitalar.dto.PlantaoRequest;

import com.hightech.escala_hospitalar.entities.Plantao;
import com.hightech.escala_hospitalar.entities.Profissional;
import com.hightech.escala_hospitalar.enuns.Categoria;
import com.hightech.escala_hospitalar.enuns.Turno;
import com.hightech.escala_hospitalar.exceptions.RegraDeNegocioException;
import com.hightech.escala_hospitalar.exceptions.ResourceNotFoundException;
import com.hightech.escala_hospitalar.repositories.PlantaoRepository;
import com.hightech.escala_hospitalar.repositories.ProfissionalRepository;
import com.hightech.escala_hospitalar.services.PlantaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PlantaoServiceTest {

    @Mock
    private PlantaoRepository plantaoRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @InjectMocks
    private PlantaoService service;

    private Profissional profissional20h;
    private final LocalDate segundaFeira = LocalDate.of(2026, 5, 18);

    @BeforeEach
    void setUp() {
        profissional20h = new Profissional(1L, "Dr. João Silva", "CRM-12345", Categoria.MEDICO, 20);
    }

    // ─── Regra 1: Não pode ter dois plantões no mesmo dia e turno ─────────────

    @Test
    @DisplayName("Deve lançar exceção quando profissional já possui plantão no mesmo dia e turno")
    void deveLancarExcecao_quandoPlantaoDuplicadoNaMesmaDiaETurno() {
        var request = new PlantaoRequest(1L, segundaFeira, Turno.MANHA);

        given(profissionalRepository.findById(1L)).willReturn(Optional.of(profissional20h));
        given(plantaoRepository.existsByProfissionalIdAndDataAndTurno(1L, segundaFeira, Turno.MANHA))
                .willReturn(true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("já possui plantão")
                .hasMessageContaining(segundaFeira.toString())
                .hasMessageContaining("MANHA");

        verify(plantaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve permitir mesmo profissional em turnos diferentes no mesmo dia")
    void devePermitir_quandoMesmoDiaMasTurnosDiferentes() {
        var requestTarde = new PlantaoRequest(1L, segundaFeira, Turno.TARDE);
        var plantaoManha = new Plantao(1L, profissional20h, segundaFeira, Turno.MANHA);
        var plantaoSalvo = new Plantao(2L, profissional20h, segundaFeira, Turno.TARDE);

        given(profissionalRepository.findById(1L)).willReturn(Optional.of(profissional20h));
        given(plantaoRepository.existsByProfissionalIdAndDataAndTurno(1L, segundaFeira, Turno.TARDE))
                .willReturn(false);
        given(plantaoRepository.findByProfissionalIdAndDataBetween(eq(1L), any(), any()))
                .willReturn(List.of(plantaoManha)); // 6h já alocada
        given(plantaoRepository.save(any())).willReturn(plantaoSalvo);

        var resultado = service.criar(requestTarde);

        assertThat(resultado).isNotNull();
        assertThat(resultado.turno()).isEqualTo(Turno.TARDE);
    }

    // ─── Regra 2: Carga horária semanal não pode ser ultrapassada ─────────────

    @Test
    @DisplayName("Deve lançar exceção quando novo plantão excederia a carga horária semanal")
    void deveLancarExcecao_quandoCargaHorariaSemanalSeriaExcedida() {
        // Profissional com 20h/semana; já alocadas 18h (3 turnos MANHA = 3 × 6h)
        // Novo plantão: NOITE = 12h → 18 + 12 = 30 > 20 → deve rejeitar
        var quintaFeira = segundaFeira.plusDays(3);
        var request = new PlantaoRequest(1L, quintaFeira, Turno.NOITE);

        var plantao1 = new Plantao(1L, profissional20h, segundaFeira, Turno.MANHA);
        var plantao2 = new Plantao(2L, profissional20h, segundaFeira.plusDays(1), Turno.MANHA);
        var plantao3 = new Plantao(3L, profissional20h, segundaFeira.plusDays(2), Turno.MANHA);

        given(profissionalRepository.findById(1L)).willReturn(Optional.of(profissional20h));
        given(plantaoRepository.existsByProfissionalIdAndDataAndTurno(1L, quintaFeira, Turno.NOITE))
                .willReturn(false);
        given(plantaoRepository.findByProfissionalIdAndDataBetween(eq(1L), any(), any()))
                .willReturn(List.of(plantao1, plantao2, plantao3));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("carga horária semanal")
                .hasMessageContaining("20");

        verify(plantaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve permitir plantão quando total de horas alocadas não excede a carga contratada")
    void devePermitir_quandoHorasAlocadasDentroDoLimite() {
        // Profissional com 20h; já tem 12h (2 MANHA × 6h); novo TARDE = 6h → total 18h < 20h
        var tercaFeira = segundaFeira.plusDays(1);
        var request = new PlantaoRequest(1L, tercaFeira, Turno.TARDE);

        var plantao1 = new Plantao(1L, profissional20h, segundaFeira, Turno.MANHA);             // 6h
        var plantao2 = new Plantao(2L, profissional20h, segundaFeira.plusDays(2), Turno.MANHA); // 6h
        // Total já alocado: 12h → + TARDE 6h = 18h <= 20h → deve permitir

        var plantaoSalvo = new Plantao(3L, profissional20h, tercaFeira, Turno.TARDE);

        given(profissionalRepository.findById(1L)).willReturn(Optional.of(profissional20h));
        given(plantaoRepository.existsByProfissionalIdAndDataAndTurno(1L, tercaFeira, Turno.TARDE))
                .willReturn(false);
        given(plantaoRepository.findByProfissionalIdAndDataBetween(eq(1L), any(), any()))
                .willReturn(List.of(plantao1, plantao2));
        given(plantaoRepository.save(any())).willReturn(plantaoSalvo);

        var resultado = service.criar(request);

        assertThat(resultado).isNotNull();
        verify(plantaoRepository).save(any(Plantao.class));
    }

    // ─── Fluxo normal ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve criar plantão com sucesso quando dados são válidos e sem conflitos")
    void deveCriarPlantao_quandoDadosValidosSemConflito() {
        var request = new PlantaoRequest(1L, segundaFeira, Turno.MANHA);
        var plantaoSalvo = new Plantao(1L, profissional20h, segundaFeira, Turno.MANHA);

        given(profissionalRepository.findById(1L)).willReturn(Optional.of(profissional20h));
        given(plantaoRepository.existsByProfissionalIdAndDataAndTurno(any(), any(), any()))
                .willReturn(false);
        given(plantaoRepository.findByProfissionalIdAndDataBetween(eq(1L), any(), any()))
                .willReturn(List.of());
        given(plantaoRepository.save(any())).willReturn(plantaoSalvo);

        var resultado = service.criar(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.turno()).isEqualTo(Turno.MANHA);
        assertThat(resultado.horaInicio()).isEqualTo("07:00");
        assertThat(resultado.horaFim()).isEqualTo("13:00");
        verify(plantaoRepository).save(any(Plantao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando profissional não é encontrado")
    void deveLancarExcecao_quandoProfissionalNaoExiste() {
        var request = new PlantaoRequest(99L, segundaFeira, Turno.MANHA);

        given(profissionalRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(plantaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar plantão inexistente")
    void deveLancarExcecao_quandoDeletarPlantaoInexistente() {
        given(plantaoRepository.existsById(999L)).willReturn(false);

        assertThatThrownBy(() -> service.deletar(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(plantaoRepository, never()).deleteById(any());
    }
}
