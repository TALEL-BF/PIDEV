package Entites;

import java.sql.Timestamp;

public class Conseil {

    private Integer idArticle;
    private String titre;
    private String contenu;
    private String categorie;
    private Timestamp dateCreation;
    private String auteur;

    public Conseil() {}

    // Insertion
    public Conseil(String titre, String contenu, String categorie, String auteur) {
        this.titre = titre;
        this.contenu = contenu;
        this.categorie = categorie;
        this.auteur = auteur;
    }

    // Lecture DB
    public Conseil(Integer idArticle, String titre, String contenu,
                   String categorie, Timestamp dateCreation, String auteur) {
        this.idArticle = idArticle;
        this.titre = titre;
        this.contenu = contenu;
        this.categorie = categorie;
        this.dateCreation = dateCreation;
        this.auteur = auteur;
    }

    public Integer getIdArticle() { return idArticle; }
    public void setIdArticle(Integer idArticle) { this.idArticle = idArticle; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    @Override
    public String toString() {
        return "Conseil{" +
                "idArticle=" + idArticle +
                ", titre='" + titre + '\'' +
                ", categorie='" + categorie + '\'' +
                ", auteur='" + auteur + '\'' +
                '}';
    }
}
