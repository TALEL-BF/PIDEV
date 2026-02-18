package Entites;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Todo implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String titre;
    private String description;
    private String statut; // "À faire", "En cours", "Terminé"
    private LocalDate dateCreation;

    public Todo() {
        this.dateCreation = LocalDate.now();
        this.statut = "À faire";
    }

    public Todo(int id, String titre, String description, String statut, LocalDate dateCreation) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.statut = statut;
        this.dateCreation = dateCreation;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    // Méthodes utilitaires
    public String getDateFormatted() {
        return dateCreation.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getIcone() {
        switch (statut) {
            case "À faire": return "⭕";
            case "En cours": return "🔄";
            case "Terminé": return "✅";
            default: return "📌";
        }
    }

    public String getCouleur() {
        switch (statut) {
            case "À faire": return "#FFA500";
            case "En cours": return "#3498DB";
            case "Terminé": return "#2ECC71";
            default: return "#95A5A6";
        }
    }
}