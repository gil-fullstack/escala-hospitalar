package com.hightech.escala_hospitalar.entities;

import com.hightech.escala_hospitalar.enuns.Categoria;
import jakarta.persistence.*;

@Entity
@Table(name = "profissionais")
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String crm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categoria categoria;

    @Column(name = "carga_horaria_semanal", nullable = false)
    private Integer cargaHorariaSemanal;

    protected Profissional() {
    }

    public Profissional(String nome, String crm, Categoria categoria, Integer cargaHorariaSemanal) {
        this.nome = nome;
        this.crm = crm;
        this.categoria = categoria;
        this.cargaHorariaSemanal = cargaHorariaSemanal;
    }

    public Profissional(Long id, String nome, String crm, Categoria categoria, Integer cargaHorariaSemanal) {
        this(nome, crm, categoria, cargaHorariaSemanal);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCrm() {
        return crm;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Integer getCargaHorariaSemanal() {
        return cargaHorariaSemanal;
    }
}
