package data;

import business.model.SeasonalRule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for SeasonalRule entity.
 * GRASP: Low Coupling â€” UI and BL never touch SQL directly.
 */
public class SeasonalRuleDAO {

    private final Connection conn;

    public SeasonalRuleDAO() throws SQLException {
        this.conn = DBConnection.getInstance().getConnection();
    }

    /** Find active rules by crop ID. */
    public List<SeasonalRule> findByCropId(String cropId) throws SQLException {
        String sql = "SELECT * FROM seasonal_rule WHERE crop_id = ? AND is_active = 1";
        List<SeasonalRule> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cropId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Find all active rules with crop names. */
    public List<SeasonalRule> findAllActive() throws SQLException {
        String sql = "SELECT sr.*, c.name AS crop_name FROM seasonal_rule sr " +
                     "JOIN crop c ON sr.crop_id = c.crop_id " +
                     "WHERE sr.is_active = 1 ORDER BY c.name, sr.start_month";
        List<SeasonalRule> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                SeasonalRule rule = mapRow(rs);
                rule.setCropName(rs.getString("crop_name"));
                list.add(rule);
            }
        }
        return list;
    }

    /** Insert a new seasonal rule. */
    public void insert(SeasonalRule rule) throws SQLException {
        String sql = "INSERT INTO seasonal_rule (rule_id, crop_id, start_month, end_month, is_active) VALUES (?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rule.getRuleId());
            ps.setString(2, rule.getCropId());
            ps.setInt(3, rule.getStartMonth());
            ps.setInt(4, rule.getEndMonth());
            ps.executeUpdate();
        }
    }

    /** Deactivate all rules for a crop. */
    public void deactivateByCropId(String cropId) throws SQLException {
        String sql = "UPDATE seasonal_rule SET is_active = 0 WHERE crop_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cropId);
            ps.executeUpdate();
        }
    }

    /** Deactivate a specific rule. */
    public void deleteById(String ruleId) throws SQLException {
        String sql = "UPDATE seasonal_rule SET is_active = 0 WHERE rule_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ruleId);
            ps.executeUpdate();
        }
    }

    /** Map ResultSet row to SeasonalRule. */
    private SeasonalRule mapRow(ResultSet rs) throws SQLException {
        SeasonalRule r = new SeasonalRule();
        r.setRuleId(rs.getString("rule_id"));
        r.setCropId(rs.getString("crop_id"));
        r.setStartMonth(rs.getInt("start_month"));
        r.setEndMonth(rs.getInt("end_month"));
        
        r.setActive(rs.getBoolean("is_active"));
        return r;
    }
}
