package Entites;

import java.sql.Timestamp;

public class Suivie {


        private int idSuivie;
        private String nomEnfant;
        private int age;
        private String nomPsy;
        private Timestamp dateSuivie;
        private int scoreHumeur;
        private int scoreStress;
        private int scoreAttention;
        private String comportement;
        private String interactionSociale;
        private String observation;
        private String statut;
        private String emailParent;
        private Integer niveauSeance;     // nullable
        private Integer idTherapieReco;   // nullable
        private String crResume;
        private String crPdfPath;


    // 🔹 Constructeur vide
        public Suivie() {
        }

        // 🔹 Constructeur sans ID (pour insertion)
        public Suivie(String nomEnfant, int age, String nomPsy,
                      Timestamp dateSuivie, int scoreHumeur, int scoreStress,
                      int scoreAttention, String comportement, String interactionSociale,
                      String observation, String statut, String emailParent, Integer niveauSeance,
                      Integer idTherapieReco, String crResume, String crPdfPath) {
            this.nomEnfant = nomEnfant;
            this.age = age;
            this.nomPsy = nomPsy;
            this.dateSuivie = dateSuivie;
            this.scoreHumeur = scoreHumeur;
            this.scoreStress = scoreStress;
            this.scoreAttention = scoreAttention;
            this.comportement = comportement;
            this.interactionSociale = interactionSociale;
            this.observation = observation;
            this.statut = statut;
            this.emailParent = emailParent;
            this.niveauSeance = niveauSeance;
            this.idTherapieReco = idTherapieReco;
            this.crResume = crResume;
            this.crPdfPath = crPdfPath;
        }

        // 🔹 Constructeur complet

        public Suivie(int idSuivie, String nomEnfant, int age, String nomPsy,
                     Timestamp dateSuivie, int scoreHumeur, int scoreStress,
                     int scoreAttention, String comportement, String interactionSociale,
                     String observation, String statut, String emailParent, Integer niveauSeance,
                     Integer idTherapieReco, String crResume, String crPdfPath) {
            this.idSuivie = idSuivie;
            this.nomEnfant = nomEnfant;
            this.age = age;
            this.nomPsy = nomPsy;
            this.dateSuivie = dateSuivie;
            this.scoreHumeur = scoreHumeur;
            this.scoreStress = scoreStress;
            this.scoreAttention = scoreAttention;
            this.comportement = comportement;
            this.interactionSociale = interactionSociale;
            this.observation = observation;
            this.statut = statut;
            this.emailParent = emailParent;
            this.niveauSeance = niveauSeance;
            this.idTherapieReco = idTherapieReco;
            this.crResume = crResume;
            this.crPdfPath = crPdfPath;
        }
// 🔹 Getters & Setters

        public int getIdSuivie() {
            return idSuivie;
        }

        public void setIdSuivie(int idSuivie) {
            this.idSuivie = idSuivie;
        }

        public String getNomEnfant() {
            return nomEnfant;
        }

        public void setNomEnfant(String nomEnfant) {
            this.nomEnfant = nomEnfant;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getNomPsy() {
            return nomPsy;
        }

        public void setNomPsy(String nomPsy) {
            this.nomPsy = nomPsy;
        }

        public Timestamp getDateSuivie() {
            return dateSuivie;
        }

        public void setDateSuivie(Timestamp dateSuivie) {
            this.dateSuivie = dateSuivie;
        }

        public int getScoreHumeur() {
            return scoreHumeur;
        }

        public void setScoreHumeur(int scoreHumeur) {
            this.scoreHumeur = scoreHumeur;
        }

        public int getScoreStress() {
            return scoreStress;
        }

        public void setScoreStress(int scoreStress) {
            this.scoreStress = scoreStress;
        }

        public int getScoreAttention() {
            return scoreAttention;
        }

        public void setScoreAttention(int scoreAttention) {
            this.scoreAttention = scoreAttention;
        }

        public String getComportement() {
            return comportement;
        }

        public void setComportement(String comportement) {
            this.comportement = comportement;
        }

        public String getInteractionSociale() {
            return interactionSociale;
        }

        public void setInteractionSociale(String interactionSociale) {
            this.interactionSociale = interactionSociale;
        }

        public String getObservation() {
            return observation;
        }

        public void setObservation(String observation) {
            this.observation = observation;
        }

        public String getStatut() {
            return statut;
        }

        public void setStatut(String statut) {
            this.statut = statut;
        }

    public String getEmailParent() { return emailParent; }
    public void setEmailParent(String emailParent) { this.emailParent = emailParent; }

    public Integer getNiveauSeance() { return niveauSeance; }
    public void setNiveauSeance(Integer niveauSeance) { this.niveauSeance = niveauSeance; }

    public Integer getIdTherapieReco() { return idTherapieReco; }
    public void setIdTherapieReco(Integer idTherapieReco) { this.idTherapieReco = idTherapieReco; }

    public String getCrResume() { return crResume; }
    public void setCrResume(String crResume) { this.crResume = crResume; }

    public String getCrPdfPath() { return crPdfPath; }
    public void setCrPdfPath(String crPdfPath) { this.crPdfPath = crPdfPath; }



    @Override
    public String toString() {
        return "Suivie{" +
                "idSuivie=" + idSuivie +
                ", nomEnfant='" + nomEnfant + '\'' +
                ", age=" + age +
                ", nomPsy='" + nomPsy + '\'' +
                ", dateSuivie=" + dateSuivie +
                ", scoreHumeur=" + scoreHumeur +
                ", scoreStress=" + scoreStress +
                ", scoreAttention=" + scoreAttention +
                ", comportement='" + comportement + '\'' +
                ", interactionSociale='" + interactionSociale + '\'' +
                ", observation='" + observation + '\'' +
                ", statut='" + statut + '\'' +
                ", emailParent='" + emailParent + '\'' +
                ", niveauSeance=" + niveauSeance +
                ", idTherapieReco=" + idTherapieReco +
                ", crResume='" + crResume + '\'' +
                ", crPdfPath='" + crPdfPath + '\'' +
                '}';
    }
}

