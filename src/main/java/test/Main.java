package test;

import Entites.Cours;
import Services.CoursServices;
import Utils.Mydatabase;

public class Main {
    public static void main(String[] args) {

        Mydatabase.getInstance();
        CoursServices cs = new CoursServices();


        Cours coursAutistes = new Cours(
                "arabe",
                "Exercices pour améliorer la communication et l'autonomie des enfants autistes",
                "Développement personnel",
                "moyen"
        );
        cs.ajouter(coursAutistes);

        // Récupérer l'ID du cours ajouté (dernier dans la liste)
        int idCoursAjoute = cs.getAll().get(cs.getAll().size() - 1).getId_cours();


       /* Cours coursModifie = new Cours(
                idCoursAjoute, // ID du cours ajouté
                "anglais",
                "Exercices avancés pour améliorer les compétences logiques et la communication",
                "Développement personnel",
                "difficile"
        );
        cs.modifier(coursModifie);
*/

        boolean supprime = cs.supprimer(idCoursAjoute);
        if (supprime) {
            System.out.println("Le cours avec ID " + idCoursAjoute + " a été supprimé !");
        } else {
            System.out.println("Erreur lors de la suppression du cours avec ID " + idCoursAjoute);
        }


        System.out.println("\nListe actuelle des cours :");
        for (Cours c : cs.getAll()) {
            System.out.println(c);
        }

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
