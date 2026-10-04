package data;

import business.model.DistributionEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for DistributionEvent entity.
 * GRASP: Low Coupling â€” UI and BL never touch SQL directly.
 */
public class DistributionDAO {

    private final Connection conn;

    public DistributionDAO() throws SQLException {
        this.conn = DBConnection.getInstance().getConnection();
    }

    /** Insert a new distribution event. */
    public void insert(DistributionEvent event) throws SQLException {
        String sql = "INSERT INTO distribution_event (dist_id, request_id, distributor_id, quantity, [timestamp]) " +
                     "VALUES (?, ?, ?, ?, GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getDistId());
            ps.setString(2, event.getRequestId());
            ps.setString(3, event.getDistributorId());
            ps.setInt(4, event.getQuantity());
            ps.executeUpdate();
        }
    }

    /** Find all distributions. */
    public List<DistributionEvent> findAll() throws SQLException {
        String sql = "SELECT * FROM distribution_event ORDER BY [timestamp] DESC";
        List<DistributionEvent> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Find distributions by distributor ID. */
    public List<DistributionEvent> findByDistributor(String distributorId) throws SQLException {
        String sql = "SELECT * FROM distribution_event WHERE distributor_id = ? ORDER BY [timestamp] DESC";
        List<DistributionEvent> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, distributorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Count total distributions. */
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM distribution_event";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Map ResultSet row to DistributionEvent. */
    private DistributionEvent mapRow(ResultSet rs) throws SQLException {
        DistributionEvent e = new DistributionEvent();
        e.setDistId(rs.getString("dist_id"));
        e.setRequestId(rs.getString("request_id"));
        e.setDistributorId(rs.getString("distributor_id"));
        e.setQuantity(rs.getInt("quantity"));
        e.setTimestamp(rs.getTimestamp("timestamp"));
        return e;
    }
}
