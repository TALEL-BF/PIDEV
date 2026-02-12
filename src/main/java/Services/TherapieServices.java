package Services;

import Entites.Therapie;
import IServices.ITherapieServices;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TherapieServices implements ITherapieServices {

    Connection con;

    public TherapieServices() {
        con = Mydatabase.getInstance().getConnection();
    }

    @Override
    public void ajouterTherapie(Therapie t) {

        String req = "INSERT INTO therapie (NOM_EXERCICE, TYPE_EXERCICE, OBJECTIF, DESCRIPTION, DUREE_MIN, NIVEAU, MATERIEL, ADAPTATION_TSA) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = con.prepareStatement(req);

            ps.setString(1, t.getNomExercice());
            ps.setString(2, t.getTypeExercice());
            ps.setString(3, t.getObjectif());
            ps.setString(4, t.getDescription());
            ps.setInt(5, t.getDureeMin());
            ps.setInt(6, t.getNiveau());
            ps.setString(7, t.getMateriel());
            ps.setString(8, t.getAdaptationTsa());

            ps.executeUpdate();
            System.out.println("Thérapie ajoutée avec succès");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void supprimerTherapie(int idTherapie) {

        String req = "DELETE FROM therapie WHERE ID_THERAPIE = ?";

        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setInt(1, idTherapie);
            ps.executeUpdate();

            System.out.println("Thérapie supprimée : ID_THERAPIE = " + idTherapie);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void modifierTherapie(Therapie t) {

        String req = "UPDATE therapie SET NOM_EXERCICE = ?, TYPE_EXERCICE = ?, OBJECTIF = ?, DESCRIPTION = ?, " +
                "DUREE_MIN = ?, NIVEAU = ?, MATERIEL = ?, ADAPTATION_TSA = ? WHERE ID_THERAPIE = ?";

        try {
            PreparedStatement ps = con.prepareStatement(req);

            ps.setString(1, t.getNomExercice());
            ps.setString(2, t.getTypeExercice());
            ps.setString(3, t.getObjectif());
            ps.setString(4, t.getDescription());
            ps.setInt(5, t.getDureeMin());
            ps.setInt(6, t.getNiveau());
            ps.setString(7, t.getMateriel());
            ps.setString(8, t.getAdaptationTsa());
            ps.setInt(9, t.getIdTherapie());

            ps.executeUpdate();
            System.out.println("Thérapie modifiée : ID_THERAPIE = " + t.getIdTherapie());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Therapie> afficherTherapie() {

        List<Therapie> list = new ArrayList<>();
        String req = "SELECT * FROM therapie";

        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {

                Therapie t = new Therapie(
                        rs.getInt("ID_THERAPIE"),
                        rs.getString("NOM_EXERCICE"),
                        rs.getString("TYPE_EXERCICE"),
                        rs.getString("OBJECTIF"),
                        rs.getString("DESCRIPTION"),
                        rs.getInt("DUREE_MIN"),
                        rs.getInt("NIVEAU"),
                        rs.getString("MATERIEL"),
                        rs.getString("ADAPTATION_TSA")
                );

                list.add(t);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }
}