package com.example.stregourist;

import java.time.LocalDateTime;

public class Evento {
    private long id;
    private String nome;
    private LocalDateTime giorno;
    private String luogo;
    private float ora_inizio;
    private float ora_fine;
    private boolean notificato = false;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDateTime getGiorno() {
        return giorno;
    }

    public void setGiorno(LocalDateTime giorno) {
        this.giorno = giorno;
    }

    public String getLuogo() {
        return luogo;
    }

    public void setLuogo(String luogo) {
        this.luogo = luogo;
    }

    public float getOra_inizio() {
        return ora_inizio;
    }

    public void setOra_inizio(float ora_inizio) {
        this.ora_inizio = ora_inizio;
    }

    public float getOra_fine() {
        return ora_fine;
    }

    public void setOra_fine(float ora_fine) {
        this.ora_fine = ora_fine;
    }

    public boolean isNotificato() {
        return notificato;
    }

    public void setNotificato(boolean notificato) {
        this.notificato = notificato;
    }
}
