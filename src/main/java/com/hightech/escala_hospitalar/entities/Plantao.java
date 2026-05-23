package com.hightech.escala_hospitalar.entities;

import com.hightech.escala_hospitalar.enuns.Turno;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "plantoes",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_plantao_profissional_data_turno",
        columnNames = {"profissional_id", "data", "turno"}
    )
)
public class Plantao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @Column(nullable = false)
    private LocalDate data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Turno turno;

    protected Plantao() {
    }

    public Plantao(Profissional profissional, LocalDate data, Turno turno) {
        this.profissional = profissional;
        this.data = data;
        this.turno = turno;
    }

    public Plantao(Long id, Profissional profissional, LocalDate data, Turno turno) {
        this(profissional, data, turno);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public LocalDate getData() {
        return data;
    }

    public Turno getTurno() {
        return turno;
    }
}
