package data;
import java.sql.*;
public class CleanAlerts {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            String sql = "WITH CTE AS (SELECT alert_id, ROW_NUMBER() OVER(PARTITION BY item_type_id ORDER BY triggered_at DESC) as rn FROM stock_alert WHERE status='ACTIVE') DELETE FROM stock_alert WHERE alert_id IN (SELECT alert_id FROM CTE WHERE rn > 1)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int rows = ps.executeUpdate();
                System.out.println("Removed " + rows + " duplicate alerts.");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
