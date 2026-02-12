package test;

import Entites.RDV;
import Services.RDVServices;import Entites.Suivie;
import Entites.Therapie;
import Services.SuivieServices;
import Services.TherapieServices;
import Utils.Mydatabase;
import java.sql.Timestamp;
import java.util.List;



public class Main {
    public static void main(String[] args) {
        Mydatabase.getInstance();

        //RDVServices rdvs = new RDVServices();

        //RDV rdv = new RDV(4,"Saleh" , "YASalih" , 25 , 2026);

        //rdvs.ajoutRDV(rdv);
        //rdvs.supprimerRDV(3);
        //rdvs.modifierRDV(rdv);
        //System.out.println(rdvs.afficherRDV());

        SuivieServices ss = new SuivieServices();
        TherapieServices ts = new TherapieServices();

        System.out.println("===== CRUD SUIVIE =====");

     /*   Suivie s = new Suivie(2,"SALIH", 9, "Dr Sami", new Timestamp(System.currentTimeMillis()), 7,
                4, 8, "CALME", "BONNE", "Bonne coopération", "EFFECTUE");
      //  ss.ajouterSuivie(s);
        System.out.println("✅ Suivie ajouté.");
        ss.modifierSuivie(s);

        //ss.supprimerSuivie(3);
        System.out.println(ss.afficherSuivie());  */

        System.out.println("\n===== CRUD THERAPIE =====");

        Therapie t = new Therapie(3,
                "GTA", "EMOTION", "Identifier joie/tristesse/colère",
                "Montrer des cartes d’émotions, demander à l’enfant de pointer et imiter.",
                15, 2, "Cartes illustrées", "Utiliser supports visuels si non verbal");
     //   ts.ajouterTherapie(t);
        System.out.println("✅ Thérapie ajoutée.");
       // ts.modifierTherapie(t);

        ts.supprimerTherapie(4);

        List<Therapie> therapies = ts.afficherTherapie();
        System.out.println("📌 Liste des exercices :");
        for (Therapie x : therapies) {
            System.out.println(x);
        }


    }
}
