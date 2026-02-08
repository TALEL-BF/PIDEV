package test;

//import Entites.RDV;
//import Services.RDVServices;
import Entites.Cours;
import Services.CoursServices;
import Utils.Mydatabase;

public class Main {
    public static void main(String[] args) {
        Mydatabase.getInstance();

        // Création du service Cours
        CoursServices cs = new CoursServices();

        // Ajout d'un cours pour l'application des autistes
        Cours coursAutistes = new Cours(
                "Communication et autonomie",
                "Exercices pour améliorer la communication et l'autonomie des enfants autistes",
                "Développement personnel",
                "Facile"
        );
        cs.ajouter(coursAutistes);

        // -----------------------------
        // Code RDV laissé en commentaire
        // -----------------------------
        //RDVServices rdvs = new RDVServices();
        //RDV rdv = new RDV(4,"Saleh" , "YASalih" , 25 , 2026);
        //rdvs.ajoutRDV(rdv);
        //rdvs.supprimerRDV(3);
        //rdvs.modifierRDV(rdv);
        //System.out.println(rdvs.afficherRDV());
    }
}
