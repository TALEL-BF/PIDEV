package Entites;

public class Therapie {

    private int idTherapie;
    private String nomExercice;
    private String typeExercice;
    private String objectif;
    private String description;
    private int dureeMin;
    private int niveau;
    private String materiel;
    private String adaptationTsa;

    public Therapie() {
    }

    public Therapie(String nomExercice, String typeExercice, String objectif,
                    String description, int dureeMin, int niveau,
                    String materiel, String adaptationTsa) {

        this.nomExercice = nomExercice;
        this.typeExercice = typeExercice;
        this.objectif = objectif;
        this.description = description;
        this.dureeMin = dureeMin;
        this.niveau = niveau;
        this.materiel = materiel;
        this.adaptationTsa = adaptationTsa;
    }

    public Therapie(int idTherapie, String nomExercice, String typeExercice,
                    String objectif, String description, int dureeMin,
                    int niveau, String materiel, String adaptationTsa) {

        this.idTherapie = idTherapie;
        this.nomExercice = nomExercice;
        this.typeExercice = typeExercice;
        this.objectif = objectif;
        this.description = description;
        this.dureeMin = dureeMin;
        this.niveau = niveau;
        this.materiel = materiel;
        this.adaptationTsa = adaptationTsa;
    }



    public int getIdTherapie() {
        return idTherapie;
    }

    public void setIdTherapie(int idTherapie) {
        this.idTherapie = idTherapie;
    }

    public String getNomExercice() {
        return nomExercice;
    }

    public void setNomExercice(String nomExercice) {
        this.nomExercice = nomExercice;
    }

    public String getTypeExercice() {
        return typeExercice;
    }

    public void setTypeExercice(String typeExercice) {
        this.typeExercice = typeExercice;
    }

    public String getObjectif() {
        return objectif;
    }

    public void setObjectif(String objectif) {
        this.objectif = objectif;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDureeMin() {
        return dureeMin;
    }

    public void setDureeMin(int dureeMin) {
        this.dureeMin = dureeMin;
    }

    public int getNiveau() {
        return niveau;
    }

    public void setNiveau(int niveau) {
        this.niveau = niveau;
    }

    public String getMateriel() {
        return materiel;
    }

    public void setMateriel(String materiel) {
        this.materiel = materiel;
    }

    public String getAdaptationTsa() {
        return adaptationTsa;
    }

    public void setAdaptationTsa(String adaptationTsa) {
        this.adaptationTsa = adaptationTsa;
    }

    @Override
    public String toString() {
        return "Therapie{" +
                "idTherapie=" + idTherapie +
                ", nomExercice='" + nomExercice + '\'' +
                ", typeExercice='" + typeExercice + '\'' +
                ", objectif='" + objectif + '\'' +
                ", description='" + description + '\'' +
                ", dureeMin=" + dureeMin +
                ", niveau=" + niveau +
                ", materiel='" + materiel + '\'' +
                ", adaptationTsa='" + adaptationTsa + '\'' +
                '}';
    }
}