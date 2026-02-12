package test;

import Entites.Cours;
import Services.CoursServices;
import Utils.Mydatabase;

public class Main {
    public static void main(String[] args) {

        // Initialisation base
        Mydatabase.getInstance();
        CoursServices cs = new CoursServices();

        // Création d'un cours avec tous les paramètres
        Cours coursAutistes = new Cours(
                "arabe",
                "Exercices pour améliorer la communication et l'autonomie des enfants autistes",
                "Développement personnel",
                "Intermédiaire",  // niveau
                10,               // durée en heures
                "Moyen"           // difficulté
        );

        cs.ajouter(coursAutistes);

        // Récupérer l'ID du cours ajouté (dernier dans la liste)
        int idCoursAjoute = cs.getAll().get(cs.getAll().size() - 1).getId_cours();

        /* Exemple de modification (si nécessaire)
        Cours coursModifie = new Cours(
                idCoursAjoute, // ID du cours ajouté
                "anglais",
                "Exercices avancés pour améliorer les compétences logiques et la communication",
                "Développement personnel",
                "Avancé",      // niveau
                12,            // durée
                "Difficile"    // difficulté
        );
        cs.modifier(coursModifie);
        */

        // Suppression
        boolean supprime = cs.supprimer(idCoursAjoute);
        if (supprime) {
            System.out.println("Le cours avec ID " + idCoursAjoute + " a été supprimé !");
        } else {
            System.out.println("Erreur lors de la suppression du cours avec ID " + idCoursAjoute);
        }

        // Affichage des cours restants
        System.out.println("\nListe actuelle des cours :");
        for (Cours c : cs.getAll()) {
            System.out.println(c);
        }

    }
}
