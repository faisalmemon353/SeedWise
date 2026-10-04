package data;
import java.sql.*;

public class MigrateDb {
    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            
            System.out.println("Starting Migration...");

            // 1. Drop the table if it exists to recreate it with the correct schema
            // (Safest way to ensure constraints and columns are perfect)
            try {
                stmt.execute("DROP TABLE IF EXISTS allocation_limit");
                System.out.println("Old allocation_limit table dropped.");
            } catch (Exception e) {
                System.out.println("Note: Could not drop table: " + e.getMessage());
            }

            // 2. Recreate the table with item_type_id
            String createSql = 
                "CREATE TABLE allocation_limit (" +
                "    limit_id     VARCHAR(36) PRIMARY KEY, " +
                "    item_type_id VARCHAR(36) NOT NULL, " +
                "    season       VARCHAR(20) NOT NULL, " +
                "    max_bags     INT NOT NULL, " +
                "    is_active    BIT DEFAULT 1, " +
                "    FOREIGN KEY (item_type_id) REFERENCES item_type(item_type_id)" +
                ")";
            stmt.execute(createSql);
            System.out.println("New allocation_limit table created with item_type_id.");

            // 3. Insert some default seed data for the 26 items
            stmt.execute("INSERT INTO allocation_limit (limit_id, item_type_id, season, max_bags, is_active) VALUES " +
                         "('lim-seed-wheat', 'seed-wheat', 'RABI', 50, 1), " +
                         "('lim-seed-rice', 'seed-rice', 'KHARIF', 40, 1), " +
                         "('lim-fert-urea', 'fert-urea', 'KHARIF', 20, 1)");
            
            System.out.println("Migration successful!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
