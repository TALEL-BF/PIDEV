package Entites;

public class Therapie {

    private int idTherapie;

    private String nomExercice;
    private String typeExercice;
    private String objectif;
    private String description;
    private int dureeMin;

    private String materiel;
    private String adaptationTsa;

    // ✅ nouveaux champs DB
    private String cible;
    private String niveauxHumeur;
    private String niveauxAttention;
    private String niveauxStresse;
    private String comportement;
    private String interaction;

    private Integer niveau; // (optionnel) si tu veux garder NIVEAU int (nullable)

    public Therapie() {}

    // ✅ constructeur pour AJOUT (sans id)
    public Therapie(String nomExercice, String typeExercice, String objectif,
                    String description, int dureeMin,
                    String materiel, String adaptationTsa,
                    String cible, String niveauxHumeur, String niveauxAttention, String niveauxStresse,
                    String comportement, String interaction,
                    Integer niveau) {

        this.nomExercice = nomExercice;
        this.typeExercice = typeExercice;
        this.objectif = objectif;
        this.description = description;
        this.dureeMin = dureeMin;
        this.materiel = materiel;
        this.adaptationTsa = adaptationTsa;

        this.cible = cible;
        this.niveauxHumeur = niveauxHumeur;
        this.niveauxAttention = niveauxAttention;
        this.niveauxStresse = niveauxStresse;
        this.comportement = comportement;
        this.interaction = interaction;

        this.niveau = niveau;
    }

    // ✅ constructeur pour AFFICHAGE/MODIF (avec id)
    public Therapie(int idTherapie, String nomExercice, String typeExercice, String objectif,
                    String description, int dureeMin,
                    String materiel, String adaptationTsa,
                    String cible, String niveauxHumeur, String niveauxAttention, String niveauxStresse,
                    String comportement, String interaction,
                    Integer niveau) {

        this(nomExercice, typeExercice, objectif, description, dureeMin,
                materiel, adaptationTsa, cible, niveauxHumeur, niveauxAttention, niveauxStresse,
                comportement, interaction, niveau);

        this.idTherapie = idTherapie;
    }

    // ===== getters/setters =====
    public int getIdTherapie() { return idTherapie; }
    public void setIdTherapie(int idTherapie) { this.idTherapie = idTherapie; }

    public String getNomExercice() { return nomExercice; }
    public void setNomExercice(String nomExercice) { this.nomExercice = nomExercice; }

    public String getTypeExercice() { return typeExercice; }
    public void setTypeExercice(String typeExercice) { this.typeExercice = typeExercice; }

    public String getObjectif() { return objectif; }
    public void setObjectif(String objectif) { this.objectif = objectif; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDureeMin() { return dureeMin; }
    public void setDureeMin(int dureeMin) { this.dureeMin = dureeMin; }

    public String getMateriel() { return materiel; }
    public void setMateriel(String materiel) { this.materiel = materiel; }

    public String getAdaptationTsa() { return adaptationTsa; }
    public void setAdaptationTsa(String adaptationTsa) { this.adaptationTsa = adaptationTsa; }

    public String getCible() { return cible; }
    public void setCible(String cible) { this.cible = cible; }

    public String getNiveauxHumeur() { return niveauxHumeur; }
    public void setNiveauxHumeur(String niveauxHumeur) { this.niveauxHumeur = niveauxHumeur; }

    public String getNiveauxAttention() { return niveauxAttention; }
    public void setNiveauxAttention(String niveauxAttention) { this.niveauxAttention = niveauxAttention; }

    public String getNiveauxStresse() { return niveauxStresse; }
    public void setNiveauxStresse(String niveauxStresse) { this.niveauxStresse = niveauxStresse; }

    public String getComportement() { return comportement; }
    public void setComportement(String comportement) { this.comportement = comportement; }

    public String getInteraction() { return interaction; }
    public void setInteraction(String interaction) { this.interaction = interaction; }

    public Integer getNiveau() { return niveau; }
    public void setNiveau(Integer niveau) { this.niveau = niveau; }

    @Override
    public String toString() {
        return "Therapie{" +
                "idTherapie=" + idTherapie +
                ", nomExercice='" + nomExercice + '\'' +
                ", typeExercice='" + typeExercice + '\'' +
                ", objectif='" + objectif + '\'' +
                ", description='" + description + '\'' +
                ", dureeMin=" + dureeMin +
                ", materiel='" + materiel + '\'' +
                ", adaptationTsa='" + adaptationTsa + '\'' +
                ", cible='" + cible + '\'' +
                ", niveauxHumeur='" + niveauxHumeur + '\'' +
                ", niveauxAttention='" + niveauxAttention + '\'' +
                ", niveauxStresse='" + niveauxStresse + '\'' +
                ", comportement='" + comportement + '\'' +
                ", interaction='" + interaction + '\'' +
                ", niveau=" + niveau +
                '}';
    }
}
