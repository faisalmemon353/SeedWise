package data;

import business.model.AllocationLimit;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for AllocationLimit entity.
 * GRASP: Low Coupling - UI and BL never touch SQL directly.
 */
public class AllocationLimitDAO {

    private final Connection conn;

    public AllocationLimitDAO() throws SQLException {
        this.conn = DBConnection.getInstance().getConnection();
    }

    /** Find active limit by item type and season. */
    public AllocationLimit findByItemAndSeason(String itemTypeId, String season) throws SQLException {
        String sql = "SELECT * FROM allocation_limit WHERE item_type_id = ? AND season = ? AND is_active = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemTypeId);
            ps.setString(2, season);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
            return null;
        }
    }

    /** Find all active limits with item names. */
    public List<AllocationLimit> findAllActive() throws SQLException {
        String sql = "SELECT al.*, it.name AS item_name FROM allocation_limit al " +
                     "JOIN item_type it ON al.item_type_id = it.item_type_id " +
                     "WHERE al.is_active = 1 ORDER BY it.name, al.season";
        List<AllocationLimit> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                AllocationLimit lim = mapRow(rs);
                lim.setItemName(rs.getString("item_name"));
                list.add(lim);
            }
        }
        return list;
    }

    /** Insert or update allocation limit. */
    public void upsert(AllocationLimit limit) throws SQLException {
        // Deactivate existing
        String deactivate = "UPDATE allocation_limit SET is_active = 0 WHERE item_type_id = ? AND season = ?";
        try (PreparedStatement ps = conn.prepareStatement(deactivate)) {
            ps.setString(1, limit.getItemTypeId());
            ps.setString(2, limit.getSeason());
            ps.executeUpdate();
        }
        // Insert new
        String sql = "INSERT INTO allocation_limit (limit_id, item_type_id, season, max_bags, is_active) VALUES (?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, limit.getLimitId());
            ps.setString(2, limit.getItemTypeId());
            ps.setString(3, limit.getSeason());
            ps.setInt(4, limit.getMaxBags());
            ps.executeUpdate();
        }
    }

    /** Map ResultSet row to AllocationLimit. */
    private AllocationLimit mapRow(ResultSet rs) throws SQLException {
        AllocationLimit l = new AllocationLimit();
        l.setLimitId(rs.getString("limit_id"));
        l.setItemTypeId(rs.getString("item_type_id"));
        l.setSeason(rs.getString("season"));
        l.setMaxBags(rs.getInt("max_bags"));
        l.setActive(rs.getBoolean("is_active"));
        return l;
    }
}
