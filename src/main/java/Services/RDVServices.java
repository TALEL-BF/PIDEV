/*package Services;

import Entites.RDV;
import IServices.IServices;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RDVServices implements IServices<RDV> {

    private Connection con;

    public RDVServices() {
        con = Mydatabase.getInstance().getConnection();
    }

    @Override
    public boolean ajouter(RDV rdv) {
        String req = "INSERT INTO RDV(nom, prenom, age, date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, rdv.getNom());
            ps.setString(2, rdv.getPrenom());
            ps.setInt(3, rdv.getAge());
            ps.setDate(4, rdv.getDate()); // rdv.getDate() doit retourner java.sql.Date

            ps.executeUpdate();
            System.out.println("RDV ajouté avec succès");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean modifier(RDV rdv) {
        String req = "UPDATE RDV SET nom = ?, prenom = ?, age = ?, date = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, rdv.getNom());
            ps.setString(2, rdv.getPrenom());
            ps.setInt(3, rdv.getAge());
            ps.setDate(4, rdv.getDate()); // java.sql.Date
            ps.setInt(5, rdv.getId());

            ps.executeUpdate();
            System.out.println("RDV avec id = " + rdv.getId() + " modifié");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean supprimer(int id) {
        String req = "DELETE FROM RDV WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("RDV avec id = " + id + " supprimé");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<RDV> getAll() {
        List<RDV> rdvs = new ArrayList<>();
        String req = "SELECT * FROM RDV";
        try (Statement ste = con.createStatement(); ResultSet rs = ste.executeQuery(req)) {
            while (rs.next()) {
                RDV rdv = new RDV(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getInt("age"),
                        rs.getDate("date") // java.sql.Date
                );
                rdvs.add(rdv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rdvs;
    }
}
*/