package Entites;

import java.time.LocalDate;
import java.util.Date;

public class Evaluation {
    private int id_evaluation;
    private String type_evaluation;
    private float score;
    private String niveau_comprehension;
    private LocalDate date_evaluation;
    private Cours cours;  // Relation avec la classe Cours

    // Constructeurs
    public Evaluation() {}

    public Evaluation(int id_evaluation, String type_evaluation, float score,
                      String niveau_comprehension, LocalDate date_evaluation, Cours cours) {
        this.id_evaluation = id_evaluation;
        this.type_evaluation = type_evaluation;
        this.score = score;
        this.niveau_comprehension = niveau_comprehension;
        this.date_evaluation = date_evaluation;
        this.cours = cours;
    }

    // Getters et Setters
    public int getId_evaluation() { return id_evaluation; }
    public void setId_evaluation(int id_evaluation) { this.id_evaluation = id_evaluation; }

    public String getType_evaluation() { return type_evaluation; }
    public void setType_evaluation(String type_evaluation) { this.type_evaluation = type_evaluation; }

    public float getScore() { return score; }
    public void setScore(float score) { this.score = score; }

    public String getNiveau_comprehension() { return niveau_comprehension; }
    public void setNiveau_comprehension(String niveau_comprehension) { this.niveau_comprehension = niveau_comprehension; }

    public LocalDate getDate_evaluation() { return date_evaluation; }
    public void setDate_evaluation(LocalDate date_evaluation) { this.date_evaluation = date_evaluation; }

    public Cours getCours() { return cours; }
    public void setCours(Cours cours) { this.cours = cours; }

    @Override
    public String toString() {
        return "Evaluation{" +
                "id_evaluation=" + id_evaluation +
                ", type_evaluation='" + type_evaluation + '\'' +
                ", score=" + score +
                ", date=" + date_evaluation +
                ", cours=" + (cours != null ? cours.getTitre() : "null") +
                '}';
    }
}