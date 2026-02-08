package Entites;

public class Cours {
    private int id_cours;
    private String titre;
    private String description;
    private String type_cours;
    private String difficulte;

    // Constructeurs
    public Cours() {
    }

    public Cours(int id_cours, String titre, String description, String type_cours, String difficulte) {
        this.id_cours = id_cours;
        this.titre = titre;
        this.description = description;
        this.type_cours = type_cours;
        this.difficulte = difficulte;
    }

    public Cours(String titre, String description, String type_cours, String difficulte) {
        this.titre = titre;
        this.description = description;
        this.type_cours = type_cours;
        this.difficulte = difficulte;
    }

    // Getters et Setters
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

    public String getDifficulte() {
        return difficulte;
    }

    public void setDifficulte(String difficulte) {
        this.difficulte = difficulte;
    }

    // toString pour affichage
    @Override
    public String toString() {
        return "Cours{" +
                "id_cours=" + id_cours +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", type_cours='" + type_cours + '\'' +
                ", difficulte='" + difficulte + '\'' +
                '}';
    }
}
