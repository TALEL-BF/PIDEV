package Entites;

public class Evaluation {
    private int id;
    private String titre;
    private String description;
    private String type_evaluation;
    private String niveau;
    private int duree_minutes;

    // Constructeurs
    public Evaluation() {
    }

    public Evaluation(int id, String titre, String description, String type_evaluation, String niveau, int duree_minutes) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.type_evaluation = type_evaluation;
        this.niveau = niveau;
        this.duree_minutes = duree_minutes;
    }

    public Evaluation(String titre, String description, String type_evaluation, String niveau, int duree_minutes) {
        this.titre = titre;
        this.description = description;
        this.type_evaluation = type_evaluation;
        this.niveau = niveau;
        this.duree_minutes = duree_minutes;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getType_evaluation() {
        return type_evaluation;
    }

    public void setType_evaluation(String type_evaluation) {
        this.type_evaluation = type_evaluation;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public int getDuree_minutes() {
        return duree_minutes;
    }

    public void setDuree_minutes(int duree_minutes) {
        this.duree_minutes = duree_minutes;
    }

    // toString pour affichage
    @Override
    public String toString() {
        return "Evaluation{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", type_evaluation='" + type_evaluation + '\'' +
                ", niveau='" + niveau + '\'' +
                ", duree_minutes=" + duree_minutes +
                '}';
    }
}