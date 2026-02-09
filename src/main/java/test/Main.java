package test;

import Entites.Event;
import Services.EventServices;
import Entites.Sponsor;
import Services.SponsorServices;
import Utils.Mydatabase;

public class Main {
    public static void main(String[] args) {

        Mydatabase.getInstance();
        // ================== TEST EVENT ==================
        System.out.println("=== TESTS EVENT ===");
        EventServices es = new EventServices();

        // 1. Ajouter un événement
        Event event1 = new Event(
                "Atelier Danse",
                "Atelier de danse pour tous les niveaux",
                "Atelier",
                "Salle Principale",
                50,
                "2026-03-01",
                "2026-03-01",
                "10:00:00",
                "12:00:00"
        );
        boolean ajoute = es.ajouter(event1);
        if (ajoute) {
            System.out.println("Événement ajouté avec succès !");
        }

        // 2. Récupérer l'ID du dernier événement ajouté
        int idEventAjoute = 0;
        if (!es.getAll().isEmpty()) {
            idEventAjoute = es.getAll().get(es.getAll().size() - 1).getIdEvent();
            System.out.println("ID de l'événement ajouté : " + idEventAjoute);
        }

        // 3. Modifier l'événement
        Event eventModifie = new Event(
                idEventAjoute,
                "Atelier Danse Avancé",
                "Atelier révisé avec chorégraphies supplémentaires",
                "Atelier",
                "Salle Principale",
                60,
                "2026-03-02",
                "2026-03-02",
                "11:00:00",
                "13:00:00"
        );
        boolean modifie = es.modifier(eventModifie);
        if (modifie) {
            System.out.println("Événement modifié avec succès !");
        }

        // 4. Afficher tous les événements
        System.out.println("\nListe des événements :");
        for (Event e : es.getAll()) {
            System.out.println(e);
        }

        // ================== TEST SPONSOR ==================
        System.out.println("=== TESTS SPONSOR ===");
        SponsorServices ss = new SponsorServices();

// 1. Ajouter un sponsor
        Sponsor sponsor1 = new Sponsor(
                "TechCorp",
                "Corporate",
                "contact@techcorp.com",
                "12345678"
        );
        boolean ajouteSponsor = ss.ajouter(sponsor1);
        if (ajouteSponsor) {
            System.out.println("Sponsor ajouté avec succès !");
        }

// 2. Récupérer l'ID du dernier sponsor ajouté
        int idSponsorAjoute = 0;
        if (!ss.getAll().isEmpty()) {
            idSponsorAjoute = ss.getAll().get(ss.getAll().size() - 1).getIdSponsor();
            System.out.println("ID du sponsor ajouté : " + idSponsorAjoute);
        }

// 3. Modifier le sponsor
        Sponsor sponsorModifie = new Sponsor(
                idSponsorAjoute,
                "TechCorp International",
                "Corporate Partner",
                "info@techcorp.com",
                "87654321"
        );
        boolean modifieSponsor = ss.modifier(sponsorModifie);
        if (modifieSponsor) {
            System.out.println("Sponsor modifié avec succès !");
        }

// 4. Afficher tous les sponsors
        System.out.println("\nListe des sponsors :");
        for (Sponsor s : ss.getAll()) {
            System.out.println(s);
        }

// 5. Supprimer un sponsor (optionnel)
/*
boolean supprimeSponsor = ss.supprimer(idSponsorAjoute);
if (supprimeSponsor) {
    System.out.println("\nSponsor avec ID " + idSponsorAjoute + " supprimé !");
} else {
    System.out.println("\nErreur lors de la suppression du sponsor");
}
*/

        // 5. Supprimer un événement (optionnel)
        /*
        boolean supprime = es.supprimer(idEventAjoute);
        if (supprime) {
            System.out.println("\nÉvénement avec ID " + idEventAjoute + " supprimé !");
        } else {
            System.out.println("\nErreur lors de la suppression de l'événement");
        }

        System.out.println("\nListe après suppression :");
        for (Event e : es.getAll()) {
            System.out.println(e);
        }
        */

       /* // ============ VOTRE PARTIE EVALUATION ============
        System.out.println("=== TESTS EVALUATION ===");
        EvaluationServices es = new EvaluationServices();

        // 1. Ajouter une évaluation
        Evaluation eval1 = new Evaluation(
                "Examen Final",
                "Examen de fin de semestre couvrant tous les chapitres",
                "Examen",
                "Avancé",
                120
        );
        boolean ajoute = es.ajouter(eval1);
        if (ajoute) {
            System.out.println("Évaluation ajoutée avec succès !");
        }

        // 2. Récupérer l'ID de la dernière évaluation ajoutée
        int idEvalAjoutee = 0;
        if (!es.getAll().isEmpty()) {
            idEvalAjoutee = es.getAll().get(es.getAll().size() - 1).getId();
            System.out.println("ID de l'évaluation ajoutée : " + idEvalAjoutee);
        }
        // 3. Modifier l'évaluation
        Evaluation evalModifiee = new Evaluation(
                idEvalAjoutee,
                "Examen Final Modifié",
                "Examen révisé avec nouveaux exercices",
                "Contrôle Continu",
                "Intermédiaire",
                90
        );
        boolean modifie = es.modifier(evalModifiee);
        if (modifie) {
            System.out.println("Évaluation modifiée avec succès !");
        }

        // 4. Afficher toutes les évaluations
        System.out.println("\nListe des évaluations :");
        for (Evaluation e : es.getAll()) {
            System.out.println(e);
        }
        */
       
        /*
        boolean supprime = es.supprimer(idEvalAjoutee);
        if (supprime) {
            System.out.println("\nÉvaluation avec ID " + idEvalAjoutee + " supprimée !");
        } else {
            System.out.println("\nErreur lors de la suppression");
        }

        System.out.println("\nListe après suppression :");
        for (Evaluation e : es.getAll()) {
            System.out.println(e);
        }
        */
       /* CoursServices cs = new CoursServices();


        Cours coursAutistes = new Cours(
                "arabe",
                "Exercices pour améliorer la communication et l'autonomie des enfants autistes",
                "Développement personnel",
                "moyen"
        );
        cs.ajouter(coursAutistes);*/

        // Récupérer l'ID du cours ajouté (dernier dans la liste)
       // int idCoursAjoute = cs.getAll().get(cs.getAll().size() - 1).getId_cours();


       /* Cours coursModifie = new Cours(
                idCoursAjoute, // ID du cours ajouté
                "anglais",
                "Exercices avancés pour améliorer les compétences logiques et la communication",
                "Développement personnel",
                "difficile"
        );
        cs.modifier(coursModifie);
*/

       /* boolean supprime = cs.supprimer(idCoursAjoute);
        if (supprime) {
            System.out.println("Le cours avec ID " + idCoursAjoute + " a été supprimé !");
        } else {
            System.out.println("Erreur lors de la suppression du cours avec ID " + idCoursAjoute);
        }


        System.out.println("\nListe actuelle des cours :");
        for (Cours c : cs.getAll()) {
            System.out.println(c);
        } */

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
