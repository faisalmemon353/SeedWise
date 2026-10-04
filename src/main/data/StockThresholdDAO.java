package data;

import business.model.StockThreshold;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for StockThreshold entity.
 * GRASP: Low Coupling - UI and BL never touch SQL directly.
 */
public class StockThresholdDAO {

    private final Connection conn;

    public StockThresholdDAO() throws SQLException {
        this.conn = DBConnection.getInstance().getConnection();
    }

    /** Find threshold by item type ID. */
    public StockThreshold findByItemTypeId(String itemTypeId) throws SQLException {
        String sql = "SELECT * FROM stock_threshold WHERE item_type_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemTypeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new StockThreshold(
                    rs.getString("threshold_id"),
                    rs.getString("item_type_id"),
                    rs.getInt("min_quantity")
                );
            }
            return null;
        }
    }

    /** Find all thresholds with item details. */
    public List<StockThreshold> findAll() throws SQLException {
        String sql = "SELECT st.*, it.name AS item_name, it.unit, ist.quantity AS current_qty " +
                     "FROM stock_threshold st " +
                     "JOIN item_type it ON st.item_type_id = it.item_type_id " +
                     "LEFT JOIN inventory_stock ist ON st.item_type_id = ist.item_type_id " +
                     "ORDER BY it.name";
        List<StockThreshold> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                StockThreshold t = new StockThreshold(
                    rs.getString("threshold_id"),
                    rs.getString("item_type_id"),
                    rs.getInt("min_quantity")
                );
                t.setItemName(rs.getString("item_name"));
                t.setUnit(rs.getString("unit"));
                t.setCurrentQty(rs.getInt("current_qty"));
                list.add(t);
            }
        }
        return list;
    }

    /** Upsert threshold (update if exists, insert if not). */
    public void upsert(StockThreshold threshold) throws SQLException {
        StockThreshold existing = findByItemTypeId(threshold.getItemTypeId());
        if (existing != null) {
            String sql = "UPDATE stock_threshold SET min_quantity = ? WHERE item_type_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, threshold.getMinQuantity());
                ps.setString(2, threshold.getItemTypeId());
                ps.executeUpdate();
            }
        } else {
            String sql = "INSERT INTO stock_threshold (threshold_id, item_type_id, min_quantity) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, threshold.getThresholdId());
                ps.setString(2, threshold.getItemTypeId());
                ps.setInt(3, threshold.getMinQuantity());
                ps.executeUpdate();
            }
        }
    }
}
