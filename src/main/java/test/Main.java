package test;

import Entites.RDV;
import Services.RDVServices;
import Utils.Mydatabase;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        Mydatabase.getInstance();

        RDVServices rdvs = new RDVServices();

        // Test with new RDV structure
        RDV rdv = new RDV(
                4,
                "suivi",
                LocalDateTime.of(2025, 3, 15, 10, 30),
                "confirme",
                45,
                1,
                1
        );

        // Test operations
        //rdvs.ajouterRDV(rdv);
        //rdvs.supprimerRDV(3);
        //rdvs.modifierRDV(rdv);
        System.out.println(rdvs.afficherRDV());
    }
}
