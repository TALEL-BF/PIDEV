package Services;

import Entites.StatsMensuelle;
import Entites.StatsSeanceDelta;
import IServices.IStatsServices;
import Utils.Mydatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StatsServices implements IStatsServices {

    private final Connection con;

    public StatsServices() {
        con = Mydatabase.getInstance().getConnection();
    }

    @Override
    public List<String> getAllEnfants() {
        List<String> enfants = new ArrayList<>();
        String sql = "SELECT DISTINCT nom_enfant FROM suivie ORDER BY nom_enfant";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                enfants.add(rs.getString(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return enfants;
    }

    @Override
    public StatsSeanceDelta getLastDelta(String nomEnfant) {
        String qLast = "SELECT * FROM suivie WHERE nom_enfant=? ORDER BY date_suivie DESC LIMIT 1";
        String qPrev = "SELECT * FROM suivie WHERE nom_enfant=? AND date_suivie < ? ORDER BY date_suivie DESC LIMIT 1";

        try (PreparedStatement psLast = con.prepareStatement(qLast)) {
            psLast.setString(1, nomEnfant);
            ResultSet rsLast = psLast.executeQuery();
            if (!rsLast.next()) return null;

            Timestamp ts = rsLast.getTimestamp("date_suivie");
            LocalDateTime lastDate = (ts == null) ? null : ts.toLocalDateTime();

            StatsSeanceDelta out = new StatsSeanceDelta();
            out.nomEnfant = nomEnfant;
            out.dateActuelle = lastDate;
            out.humeurActuelle = rsLast.getInt("score_humeur");
            out.stressActuel = rsLast.getInt("score_stress");
            out.attentionActuelle = rsLast.getInt("score_attention");

            try (PreparedStatement psPrev = con.prepareStatement(qPrev)) {
                psPrev.setString(1, nomEnfant);
                psPrev.setTimestamp(2, ts);

                ResultSet rsPrev = psPrev.executeQuery();
                if (rsPrev.next()) {
                    out.prevHumeur = rsPrev.getInt("score_humeur");
                    out.prevStress = rsPrev.getInt("score_stress");
                    out.prevAttention = rsPrev.getInt("score_attention");
                }
            }

            return out;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<StatsMensuelle> getMonthlyStats(String nomEnfant) {
        String sql =
                "SELECT YEAR(date_suivie) y, MONTH(date_suivie) m, " +
                        "AVG(score_humeur) avg_h, AVG(score_stress) avg_s, AVG(score_attention) avg_a, " +
                        "COUNT(*) nb " +
                        "FROM suivie WHERE nom_enfant=? " +
                        "GROUP BY YEAR(date_suivie), MONTH(date_suivie) " +
                        "ORDER BY y, m";

        List<StatsMensuelle> list = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nomEnfant);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                StatsMensuelle m = new StatsMensuelle();
                m.year = rs.getInt("y");
                m.month = rs.getInt("m");
                m.avgHumeur = rs.getDouble("avg_h");
                m.avgStress = rs.getDouble("avg_s");
                m.avgAttention = rs.getDouble("avg_a");
                m.nbSeances = rs.getInt("nb");
                list.add(m);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }
}
