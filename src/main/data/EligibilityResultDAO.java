package data;

import business.model.EligibilityResult;
import java.sql.*;

/**
 * Data Access Object for EligibilityResult entity.
 * GRASP: Low Coupling - UI and BL never touch SQL directly.
 */
public class EligibilityResultDAO {

    private final Connection conn;

    public EligibilityResultDAO() throws SQLException {
        this.conn = DBConnection.getInstance().getConnection();
    }

    /** Insert or update eligibility result for a request. */
    public void upsert(EligibilityResult result, String requestId) throws SQLException {
        // Delete existing result for this request
        String del = "DELETE FROM eligibility_result WHERE request_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(del)) {
            ps.setString(1, requestId);
            ps.executeUpdate();
        }
        // Insert new
        String sql = "INSERT INTO eligibility_result (result_id, request_id, result, reason, checked_at) " +
                     "VALUES (?, ?, ?, ?, GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, result.getResultId());
            ps.setString(2, requestId);
            ps.setBoolean(3, result.isEligible());
            ps.setString(4, result.getReason());
            ps.executeUpdate();
        }
    }

    /** Find by request ID. */
    public EligibilityResult findByRequestId(String requestId) throws SQLException {
        String sql = "SELECT * FROM eligibility_result WHERE request_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, requestId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                EligibilityResult r = new EligibilityResult();
                r.setResultId(rs.getString("result_id"));
                r.setRequestId(rs.getString("request_id"));
                r.setResult(rs.getBoolean("result"));
                r.setReason(rs.getString("reason"));
                r.setCheckedAt(rs.getTimestamp("checked_at"));
                return r;
            }
            return null;
        }
    }
}
