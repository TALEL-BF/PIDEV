package test;

import Entites.Therapie;
import Services.SuivieServices;
import Services.TherapieServices;
import Utils.Mydatabase;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Mydatabase.getInstance();

        SuivieServices ss = new SuivieServices();
        TherapieServices ts = new TherapieServices();

        System.out.println("===== CRUD THERAPIE =====");

        // Exemple affichage
        List<Therapie> therapies = ts.afficherTherapie();
        System.out.println("📌 Liste des exercices (" + therapies.size() + "):");
        for (Therapie x : therapies) {
            System.out.println(x);
        }

        // Exemple delete (attention: vérifie l'id avant)
        // ts.supprimerTherapie(4);
        // System.out.println("✅ Thérapie supprimée (ID=4)");
    }
}
