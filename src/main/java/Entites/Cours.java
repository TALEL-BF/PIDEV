package Entites;

public class Cours {

    private int id_cours;
    private String titre;
    private String description;
    private String type_cours;
    private String niveau;
    private int duree;
    private String image;  // Changé de 'difficulte' à 'image'

    // ================== Constructeur vide ==================
    public Cours() {
    }

    // ================== Constructeur complet ==================
    public Cours(int id_cours, String titre, String description,
                 String type_cours, String niveau,
                 int duree, String image) {
        this.id_cours = id_cours;
        this.titre = titre;
        this.description = description;
        this.type_cours = type_cours;
        this.niveau = niveau;
        this.duree = duree;
        this.image = image;
    }

    // ================== Constructeur sans id (pour ajout) ==================
    public Cours(String titre, String description,
                 String type_cours, String niveau,
                 int duree, String image) {
        this.titre = titre;
        this.description = description;
        this.type_cours = type_cours;
        this.niveau = niveau;
        this.duree = duree;
        this.image = image;
    }

    // ================== Getters & Setters ==================

    public int getId_cours() {
        return id_cours;
    }

    public void setId_cours(int id_cours) {
        this.id_cours = id_cours;
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

    public String getType_cours() {
        return type_cours;
    }

    public void setType_cours(String type_cours) {
        this.type_cours = type_cours;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    // ================== toString ==================
    @Override
    public String toString() {
        return "Cours{" +
                "id_cours=" + id_cours +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", type_cours='" + type_cours + '\'' +
                ", niveau='" + niveau + '\'' +
                ", duree=" + duree +
                ", image='" + image + '\'' +
                '}';
    }
}