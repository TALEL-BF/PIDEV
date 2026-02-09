package Entites;
public class Event {

    private int idEvent;
    private String titre;
    private String description;
    private String typeEvent;
    private String lieu;
    private int maxParticipant;
    private String dateDebut;
    private String dateFin;
    private String heureDebut;
    private String heureFin;

    // Constructeur vide
    public Event() {
    }

    // Constructeur avec ID
    public Event(int idEvent, String titre, String description, String typeEvent,
                 String lieu, int maxParticipant,
                 String dateDebut, String dateFin,
                 String heureDebut, String heureFin) {
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
    }

    // Constructeur sans ID
    public Event(String titre, String description, String typeEvent,
                 String lieu, int maxParticipant,
                 String dateDebut, String dateFin,
                 String heureDebut, String heureFin) {
        this.titre = titre;
        this.description = description;
        this.typeEvent = typeEvent;
        this.lieu = lieu;
        this.maxParticipant = maxParticipant;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    // Getters et Setters
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

    public String getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(String dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getDateFin() {
        return dateFin;
    }

    public void setDateFin(String dateFin) {
        this.dateFin = dateFin;
    }

    public String getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
    }

    public String getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(String heureFin) {
        this.heureFin = heureFin;
    }

    // toString
    @Override
    public String toString() {
        return "Event{" +
                "idEvent=" + idEvent +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", typeEvent='" + typeEvent + '\'' +
                ", lieu='" + lieu + '\'' +
                ", maxParticipant=" + maxParticipant +
                ", dateDebut='" + dateDebut + '\'' +
                ", dateFin='" + dateFin + '\'' +
                ", heureDebut='" + heureDebut + '\'' +
                ", heureFin='" + heureFin + '\'' +
                '}';
    }
}
