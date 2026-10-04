package data;

import business.model.AuditTrail;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for AuditTrail entity.
 * GRASP: Low Coupling â€” UI and BL never touch SQL directly.
 */
public class AuditTrailDAO {

    private final Connection conn;

    public AuditTrailDAO() throws SQLException {
        this.conn = DBConnection.getInstance().getConnection();
    }

    /** Insert a new audit trail entry. */
    public void insert(AuditTrail entry) throws SQLException {
        String sql = "INSERT INTO audit_trail (audit_id, entity_id, action, actor_id, actor_role, [timestamp], details) " +
                     "VALUES (?, ?, ?, ?, ?, GETDATE(), ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entry.getAuditId());
            ps.setString(2, entry.getEntityId());
            ps.setString(3, entry.getAction());
            ps.setString(4, entry.getActorId());
            ps.setString(5, entry.getActorRole());
            ps.setString(6, entry.getDetails());
            ps.executeUpdate();
        }
    }

    /** Find all audit entries by entity ID. */
    public List<AuditTrail> findByEntityId(String entityId) throws SQLException {
        String sql = "SELECT * FROM audit_trail WHERE entity_id = ? ORDER BY [timestamp] DESC";
        return executeQuery(sql, entityId);
    }

    /** Find all audit entries by role. */
    public List<AuditTrail> findByRole(String role) throws SQLException {
        String sql = "SELECT TOP 100 * FROM audit_trail WHERE actor_role = ? ORDER BY [timestamp] DESC";
        return executeQuery(sql, role);
    }

    /** Find recent audit entries. */
    public List<AuditTrail> findRecent(int limit) throws SQLException {
        String sql = "SELECT TOP (?) * FROM audit_trail ORDER BY [timestamp] DESC";
        List<AuditTrail> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Helper query with single string param. */
    private List<AuditTrail> executeQuery(String sql, String param) throws SQLException {
        List<AuditTrail> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Map ResultSet row to AuditTrail. */
    private AuditTrail mapRow(ResultSet rs) throws SQLException {
        AuditTrail a = new AuditTrail();
        a.setAuditId(rs.getString("audit_id"));
        a.setEntityId(rs.getString("entity_id"));
        a.setAction(rs.getString("action"));
        a.setActorId(rs.getString("actor_id"));
        a.setActorRole(rs.getString("actor_role"));
        a.setTimestamp(rs.getTimestamp("timestamp"));
        a.setDetails(rs.getString("details"));
        return a;
    }
}
