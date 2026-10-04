package data;

import business.model.Report;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Report entity.
 * GRASP: Low Coupling â€” UI and BL never touch SQL directly.
 */
public class ReportDAO {

    private final Connection conn;

    public ReportDAO() throws SQLException {
        this.conn = DBConnection.getInstance().getConnection();
    }

    /** Insert a new report record. */
    public void insert(Report report) throws SQLException {
        String sql = "INSERT INTO report (report_id, officer_id, distributor_id, type, generated_at, filters) " +
                     "VALUES (?, ?, ?, ?, GETDATE(), ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, report.getReportId());
            ps.setString(2, report.getOfficerId());
            ps.setString(3, report.getDistributorId());
            ps.setString(4, report.getType());
            ps.setString(5, report.getFilters());
            ps.executeUpdate();
        }
    }

    /** Find all reports. */
    public List<Report> findAll() throws SQLException {
        String sql = "SELECT * FROM report ORDER BY generated_at DESC";
        List<Report> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Count all reports. */
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM report";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Map ResultSet row to Report. */
    private Report mapRow(ResultSet rs) throws SQLException {
        Report r = new Report();
        r.setReportId(rs.getString("report_id"));
        r.setOfficerId(rs.getString("officer_id"));
        r.setDistributorId(rs.getString("distributor_id"));
        r.setType(rs.getString("type"));
        r.setGeneratedAt(rs.getTimestamp("generated_at"));
        r.setFilters(rs.getString("filters"));
        return r;
    }
}
