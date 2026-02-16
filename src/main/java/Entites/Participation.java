package com.auticare.entities;

import java.time.LocalDateTime;

public class Participation {

    private int idParticipation;
    private int idUser;
    private int idEvent;
    private LocalDateTime dateParticipation;
    private int nbrP;  // ← AJOUT: nombre de participants réservés

    // Constructeur vide
    public Participation() {}

    // Constructeur sans id (avec nbrP)
    public Participation(int idUser, int idEvent, LocalDateTime dateParticipation, int nbrP) {
        this.idUser = idUser;
        this.idEvent = idEvent;
        this.dateParticipation = dateParticipation;
        this.nbrP = nbrP;  // ← AJOUT
    }

    // Constructeur avec id (avec nbrP)
    public Participation(int idParticipation, int idUser, int idEvent, LocalDateTime dateParticipation, int nbrP) {
        this.idParticipation = idParticipation;
        this.idUser = idUser;
        this.idEvent = idEvent;
        this.dateParticipation = dateParticipation;
        this.nbrP = nbrP;  // ← AJOUT
    }

    // ================= GETTERS & SETTERS =================

    public int getIdParticipation() {
        return idParticipation;
    }

    public void setIdParticipation(int idParticipation) {
        this.idParticipation = idParticipation;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int getIdEvent() {
        return idEvent;
    }

    public void setIdEvent(int idEvent) {
        this.idEvent = idEvent;
    }

    public LocalDateTime getDateParticipation() {
        return dateParticipation;
    }

    public void setDateParticipation(LocalDateTime dateParticipation) {
        this.dateParticipation = dateParticipation;
    }

    // ================= NOUVEAU GETTER/SETTER pour nbrP =================
    public int getNbrP() {
        return nbrP;
    }

    public void setNbrP(int nbrP) {
        this.nbrP = nbrP;
    }

    // ================= toString =================

    @Override
    public String toString() {
        return "Participation{" +
                "idParticipation=" + idParticipation +
                ", idUser=" + idUser +
                ", idEvent=" + idEvent +
                ", dateParticipation=" + dateParticipation +
                ", nbrP=" + nbrP +  // ← AJOUT
                '}';
    }
}