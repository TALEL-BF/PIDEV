package test;

import Entites.Cours;
import Services.CoursServices;
import Utils.Mydatabase;

public class Main {
    public static void main(String[] args) {

        // Initialisation base
        Mydatabase.getInstance();
        CoursServices cs = new CoursServices();

        // Création d'un cours avec tous les paramètres (y compris mots et images_mots)
        Cours coursAutistes = new Cours(
                "arabe",                    // titre
                "Exercices pour améliorer la communication et l'autonomie des enfants autistes", // description
                "Développement personnel",  // type_cours
                "Intermédiaire",             // niveau
                10,                          // duree
                "Moyen",                     // image
                "",                          // mots (vide pour l'instant)
                ""                           // images_mots (vide pour l'instant)
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
                "Difficile",   // image
                "",            // mots
                ""             // images_mots
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