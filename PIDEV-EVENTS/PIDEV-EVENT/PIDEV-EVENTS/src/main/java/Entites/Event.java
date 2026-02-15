package Entites;

import java.sql.Date;
import java.sql.Time;

public class Event {

    private int idEvent;
    private String titre;
    private String description;
    private String typeEvent;
    private String lieu;
    private int maxParticipant;
    private Date dateDebut;
    private Date dateFin;
    private Time heureDebut;
    private Time heureFin;

    // ⭐ NEW FIELD (image path or URL)
    private String image;

    // Constructor vide
    public Event() {}

    // Constructor sans id
    public Event(String titre, String description, String typeEvent, String lieu,
                 int maxParticipant, Date dateDebut, Date dateFin,
                 Time heureDebut, Time heureFin, String image) {

        this.titre = titre;
        this.description = description;
        this.typeEvent = typeEvent;
        this.lieu = lieu;
        this.maxParticipant = maxParticipant;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.image = image;
    }

    // Constructor avec id
    public Event(int idEvent, String titre, String description, String typeEvent, String lieu,
                 int maxParticipant, Date dateDebut, Date dateFin,
                 Time heureDebut, Time heureFin, String image) {

        this.idEvent = idEvent;
        this.titre = titre;
        this.description = description;
        this.typeEvent = typeEvent;
        this.lieu = lieu;
        this.maxParticipant = maxParticipant;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.image = image;
    }

    // ================= GETTERS & SETTERS =================

    public int getIdEvent() {
        return idEvent;
    }

    public void setIdEvent(int idEvent) {
        this.idEvent = idEvent;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTypeEvent() {
        return typeEvent;
    }

    public void setTypeEvent(String typeEvent) {
        this.typeEvent = typeEvent;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public int getMaxParticipant() {
        return maxParticipant;
    }

    public void setMaxParticipant(int maxParticipant) {
        this.maxParticipant = maxParticipant;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public Time getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(Time heureDebut) {
        this.heureDebut = heureDebut;
    }

    public Time getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(Time heureFin) {
        this.heureFin = heureFin;
    }

    // ⭐ IMAGE GETTER / SETTER
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    // ================= toString =================

    @Override
    public String toString() {
        return "Event{" +
                "idEvent=" + idEvent +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", typeEvent='" + typeEvent + '\'' +
                ", lieu='" + lieu + '\'' +
                ", maxParticipant=" + maxParticipant +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", heureDebut=" + heureDebut +
                ", heureFin=" + heureFin +
                ", image='" + image + '\'' +
                '}';
    }
}
