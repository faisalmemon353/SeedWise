package data;
import java.sql.Connection;
import java.sql.Statement;
public class AlterTableYears {
    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            try { stmt.executeUpdate("ALTER TABLE seasonal_rule ADD start_year INT"); } catch(Exception e) {}
            try { stmt.executeUpdate("ALTER TABLE seasonal_rule ADD end_year INT"); } catch(Exception e) {}
            try { stmt.executeUpdate("UPDATE seasonal_rule SET start_year = 2025, end_year = 2026 WHERE start_year IS NULL"); } catch(Exception e) {}
            System.out.println("Alter successful.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
