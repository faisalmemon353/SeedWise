package data;

import business.model.StockAlert;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for StockAlert entity.
 * GRASP: Low Coupling - UI and BL never touch SQL directly.
 */
public class StockAlertDAO {

    private final Connection conn;

    public StockAlertDAO() throws SQLException {
        this.conn = DBConnection.getInstance().getConnection();
    }

    /** Check if an active alert already exists for the item. */
    public boolean hasActiveAlert(String itemTypeId) throws SQLException {
        String sql = "SELECT 1 FROM stock_alert WHERE item_type_id = ? AND status = 'ACTIVE'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemTypeId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    /** Create a new low-stock alert. */
    public void createAlert(String alertId, String itemTypeId) throws SQLException {
        String sql = "INSERT INTO stock_alert (alert_id, item_type_id, triggered_at, status) " +
                     "VALUES (?, ?, GETDATE(), 'ACTIVE')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, alertId);
            ps.setString(2, itemTypeId);
            ps.executeUpdate();
        }
    }

    /** Find all active alerts with item details. */
    public List<StockAlert> findActive() throws SQLException {
        String sql = "SELECT sa.*, it.name AS item_name, it.unit, " +
                     "ist.quantity AS current_qty, st.min_quantity " +
                     "FROM stock_alert sa " +
                     "JOIN item_type it ON sa.item_type_id = it.item_type_id " +
                     "LEFT JOIN inventory_stock ist ON sa.item_type_id = ist.item_type_id " +
                     "LEFT JOIN stock_threshold st ON sa.item_type_id = st.item_type_id " +
                     "WHERE sa.status = 'ACTIVE' ORDER BY sa.triggered_at DESC";
        List<StockAlert> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Acknowledge an alert. */
    public void acknowledge(String alertId, String note) throws SQLException {
        String sql = "UPDATE stock_alert SET status = 'ACKNOWLEDGED', note = ? WHERE alert_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, note);
            ps.setString(2, alertId);
            ps.executeUpdate();
        }
    }

    /** Map ResultSet row to StockAlert. */
    private StockAlert mapRow(ResultSet rs) throws SQLException {
        StockAlert a = new StockAlert();
        a.setAlertId(rs.getString("alert_id"));
        a.setItemTypeId(rs.getString("item_type_id"));
        a.setTriggeredAt(rs.getTimestamp("triggered_at"));
        a.setStatus(rs.getString("status"));
        a.setNote(rs.getString("note"));
        a.setItemName(rs.getString("item_name"));
        a.setUnit(rs.getString("unit"));
        a.setCurrentQty(rs.getInt("current_qty"));
        a.setMinQuantity(rs.getInt("min_quantity"));
        return a;
    }
}
