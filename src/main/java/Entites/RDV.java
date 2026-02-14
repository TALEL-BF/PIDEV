package Entites;

import java.time.LocalDateTime;

public class RDV {
    private int idRdv;
    private String typeConsultation; // premiere_consultation, suivi, urgence, familiale, bilan
    private LocalDateTime dateHeureRdv;
    private String statutRdv; // planifiee, confirme, annule, reporte, termine
    private int dureeRdvMinutes;
    private int idPsychologue;
    private int idAutiste;

    // Constructors
    public RDV() {}

    public RDV(int idRdv, String typeConsultation, LocalDateTime dateHeureRdv,
               String statutRdv, int dureeRdvMinutes, int idPsychologue, int idAutiste) {
        this.idRdv = idRdv;
        this.typeConsultation = typeConsultation;
        this.dateHeureRdv = dateHeureRdv;
        this.statutRdv = statutRdv;
        this.dureeRdvMinutes = dureeRdvMinutes;
        this.idPsychologue = idPsychologue;
        this.idAutiste = idAutiste;
    }

    public RDV(String typeConsultation, LocalDateTime dateHeureRdv,
               String statutRdv, int dureeRdvMinutes, int idPsychologue, int idAutiste) {
        this.typeConsultation = typeConsultation;
        this.dateHeureRdv = dateHeureRdv;
        this.statutRdv = statutRdv;
        this.dureeRdvMinutes = dureeRdvMinutes;
        this.idPsychologue = idPsychologue;
        this.idAutiste = idAutiste;
    }

    // Getters and Setters
    public int getIdRdv() {
        return idRdv;
    }

    public void setIdRdv(int idRdv) {
        this.idRdv = idRdv;
    }

    public String getTypeConsultation() {
        return typeConsultation;
    }

    public void setTypeConsultation(String typeConsultation) {
        this.typeConsultation = typeConsultation;
    }

    public LocalDateTime getDateHeureRdv() {
        return dateHeureRdv;
    }

    public void setDateHeureRdv(LocalDateTime dateHeureRdv) {
        this.dateHeureRdv = dateHeureRdv;
    }

    public String getStatutRdv() {
        return statutRdv;
    }

    public void setStatutRdv(String statutRdv) {
        this.statutRdv = statutRdv;
    }

    public int getDureeRdvMinutes() {
        return dureeRdvMinutes;
    }

    public void setDureeRdvMinutes(int dureeRdvMinutes) {
        this.dureeRdvMinutes = dureeRdvMinutes;
    }

    public int getIdPsychologue() {
        return idPsychologue;
    }

    public void setIdPsychologue(int idPsychologue) {
        this.idPsychologue = idPsychologue;
    }

    public int getIdAutiste() {
        return idAutiste;
    }

    public void setIdAutiste(int idAutiste) {
        this.idAutiste = idAutiste;
    }

    @Override
    public String toString() {
        return "RDV{" +
                "idRdv=" + idRdv +
                ", typeConsultation='" + typeConsultation + '\'' +
                ", dateHeureRdv=" + dateHeureRdv +
                ", statutRdv='" + statutRdv + '\'' +
                '}';
    }
}
