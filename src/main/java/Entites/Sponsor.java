package Entites;

public class Sponsor {

    private int idSponsor;
    private String nom;
    private String typeSponsor;
    private String email;
    private String telephone;

    // Constructeur vide
    public Sponsor() {
    }

    // Constructeur avec ID
    public Sponsor(int idSponsor, String nom, String typeSponsor,
                   String email, String telephone) {
        this.idSponsor = idSponsor;
        this.nom = nom;
        this.typeSponsor = typeSponsor;
        this.email = email;
        this.telephone = telephone;
    }

    // Constructeur sans ID
    public Sponsor(String nom, String typeSponsor,
                   String email, String telephone) {
        this.nom = nom;
        this.typeSponsor = typeSponsor;
        this.email = email;
        this.telephone = telephone;
    }

    // Getters et Setters
    public int getIdSponsor() {
        return idSponsor;
    }

    public void setIdSponsor(int idSponsor) {
        this.idSponsor = idSponsor;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTypeSponsor() {
        return typeSponsor;
    }

    public void setTypeSponsor(String typeSponsor) {
        this.typeSponsor = typeSponsor;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    // toString
    @Override
    public String toString() {
        return "Sponsor{" +
                "idSponsor=" + idSponsor +
                ", nom='" + nom + '\'' +
                ", typeSponsor='" + typeSponsor + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                '}';
    }
}
