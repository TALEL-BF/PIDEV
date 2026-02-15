package Entites;

public class Sponsor {

    private int idSponsor;
    private String nom;
    private String typeSponsor;
    private String email;
    private int telephone;
    private String description;
    private String image;  // ⭐ NOUVEAU CHAMP IMAGE

    // Constructors
    public Sponsor() {}

    // Constructor avec id et image
    public Sponsor(int idSponsor, String nom, String typeSponsor, String email, int telephone, String description, String image) {
        this.idSponsor = idSponsor;
        this.nom = nom;
        this.typeSponsor = typeSponsor;
        this.email = email;
        this.telephone = telephone;
        this.description = description;
        this.image = image;
    }

    // Constructor sans id avec image
    public Sponsor(String nom, String typeSponsor, String email, int telephone, String description, String image) {
        this.nom = nom;
        this.typeSponsor = typeSponsor;
        this.email = email;
        this.telephone = telephone;
        this.description = description;
        this.image = image;
    }
    public Sponsor(int idSponsor, String nom, String typeSponsor, String email, int telephone, String description) {
        this.idSponsor = idSponsor;
        this.nom = nom;
        this.typeSponsor = typeSponsor;
        this.email = email;
        this.telephone = telephone;
        this.description = description;
        this.image = null;
    }

    // Constructor sans id sans image (pour compatibilité)
    public Sponsor(String nom, String typeSponsor, String email, int telephone, String description) {
        this.nom = nom;
        this.typeSponsor = typeSponsor;
        this.email = email;
        this.telephone = telephone;
        this.description = description;
        this.image = null;
    }

    // Getters & Setters
    public int getIdSponsor() { return idSponsor; }
    public void setIdSponsor(int idSponsor) { this.idSponsor = idSponsor; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getTypeSponsor() { return typeSponsor; }
    public void setTypeSponsor(String typeSponsor) { this.typeSponsor = typeSponsor; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getTelephone() { return telephone; }
    public void setTelephone(int telephone) { this.telephone = telephone; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // ⭐ NOUVEAU getter/setter pour image
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    @Override
    public String toString() {
        return "Sponsor{" +
                "idSponsor=" + idSponsor +
                ", nom='" + nom + '\'' +
                ", typeSponsor='" + typeSponsor + '\'' +
                ", email='" + email + '\'' +
                ", telephone=" + telephone +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +  // ⭐ AJOUTÉ
                '}';
    }
}