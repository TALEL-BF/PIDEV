package Entites;

import java.sql.Date;

public class RDV {
    private int id;
    private String nom;
    private String prenom;
    private int age;
    private int date;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public RDV() {
    }

    public RDV(int id, String nom, String prenom, int age, int date) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
        this.date = date;
    }

    public RDV(String nom, String prenom, int age, int date) {
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
        this.date = date;
    }

    @Override
    public String toString() {
        return "RDV{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", age=" + age +
                ", date=" + date +
                '}';
    }
}
