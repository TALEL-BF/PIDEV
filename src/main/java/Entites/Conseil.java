package Entites;

import java.sql.Timestamp;

public class Conseil {

    private Integer idArticle;
    private String titre;
    private String contenu;
    private String categorie;
    private Timestamp dateCreation;
    private String auteur;
    private String auteurImage;


    // ✅ NEW
    private int likesCount;

    public Conseil() {}

    // Insertion
    public Conseil(String titre, String contenu, String categorie, String auteur, String auteurImage) {
        this.titre = titre;
        this.contenu = contenu;
        this.categorie = categorie;
        this.auteur = auteur;
        this.auteurImage = auteurImage;
    }

    // Lecture DB (avec likes_count)
    public Conseil(Integer idArticle, String titre, String contenu,
                   String categorie, Timestamp dateCreation, String auteur, String auteurImage, int likesCount) {
        this.idArticle = idArticle;
        this.titre = titre;
        this.contenu = contenu;
        this.categorie = categorie;
        this.dateCreation = dateCreation;
        this.auteur = auteur;
        this.auteurImage = auteurImage;
        this.likesCount = likesCount;
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

    // ✅ NEW
    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }
    public String getAuteurImage() { return auteurImage; }
    public void setAuteurImage(String auteurImage) { this.auteurImage = auteurImage; }

    @Override
    public String toString() {
        return "Conseil{" +
                "idArticle=" + idArticle +
                ", titre='" + titre + '\'' +
                ", categorie='" + categorie + '\'' +
                ", auteur='" + auteur + '\'' +
                ", likesCount=" + likesCount +
                '}';
    }
}