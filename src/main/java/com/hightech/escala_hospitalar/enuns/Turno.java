package com.hightech.escala_hospitalar.enuns;

public enum Turno {

    MANHA(6, "07:00", "13:00"),
    TARDE(6, "13:00", "19:00"),
    NOITE(12, "19:00", "07:00");

    private final int duracaoHoras;
    private final String horaInicio;
    private final String horaFim;

    Turno(int duracaoHoras, String horaInicio, String horaFim) {
        this.duracaoHoras = duracaoHoras;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public int getDuracaoHoras() {
        return duracaoHoras;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public String getHoraFim() {
        return horaFim;
    }
}
