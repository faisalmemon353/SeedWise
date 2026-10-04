package data;
import java.sql.*;

public class CheckDb {
    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "allocation_limit", null);
            System.out.println("Columns in allocation_limit:");
            while (rs.next()) {
                System.out.println("- " + rs.getString("COLUMN_NAME"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
