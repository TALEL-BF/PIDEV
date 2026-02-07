package Services;

import Entites.RDV;
import IServices.IRDVServices;
import Utils.Mydatabase;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;

public class RDVServices implements IRDVServices {

    Connection con;
  public   RDVServices(){
        con= Mydatabase.getInstance().getConnection();
    }

    @Override
    public void ajoutRDV(RDV rdv) {

        String req = "INSERT INTO RDV(nom, prenom, age, date) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = con.prepareStatement(req);
            ps.setString(1, rdv.getNom());
            ps.setString(2, rdv.getPrenom());
            ps.setInt(3, rdv.getAge());
            ps.setInt(4, rdv.getDate());

            ps.executeUpdate();
            System.out.println("RDV ajouté avec succès");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void supprimerRDV(int id) {
      String req = "DELETE FROM RDV WHERE id = ?";
        try {
            PreparedStatement ps=con.prepareStatement(req);
            ps.setInt(1,id);
            ps.executeUpdate();
            System.out.println("user with id = "+id+" is deleted");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void modifierRDV(RDV rdv) {
      String req="Update RDV SET prenom = ? where id = ?";

        try {
            PreparedStatement ps=con.prepareStatement(req);
            ps.setString(1, rdv.getPrenom());
            ps.setInt(2, rdv.getId());
            ps.executeUpdate();
            System.out.println("user with id = "+rdv.getId()+" is modified");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<RDV> afficherRDV() {
      List<RDV> rdvs = new ArrayList<>();
      String  req = "SELECT * FROM RDV";
        try {
            Statement ste = con.createStatement();
            ResultSet rs = ste.executeQuery(req);
            while (rs.next()) {
                RDV rdv = new RDV(rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"), rs.getInt("age"), rs.getInt("date"));
            rdvs.add(rdv);
            }
        } catch  (SQLException e) {
            throw new RuntimeException(e);
        }
        return rdvs;
    }
}
