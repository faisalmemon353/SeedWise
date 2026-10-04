package data;

import business.model.SubsidyRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for SubsidyRequest entity.
 * GRASP: Low Coupling â€” UI and BL never touch SQL directly.
 */
public class SubsidyRequestDAO {

    private final Connection conn;

    public SubsidyRequestDAO() throws SQLException {
        this.conn = DBConnection.getInstance().getConnection();
    }

    /** Insert a new subsidy request. */
    public void insert(SubsidyRequest req) throws SQLException {
        String sql = "INSERT INTO subsidy_request (request_id, farmer_id, crop_id, status, quantity, request_date, season, requested_item_id) " +
                     "VALUES (?, ?, ?, ?, ?, GETDATE(), ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, req.getRequestId());
            ps.setString(2, req.getFarmerId());
            ps.setString(3, req.getCropId());
            ps.setString(4, req.getStatus());
            ps.setInt(5, req.getQuantity());
            ps.setString(6, req.getSeason());
            ps.setString(7, req.getRequestedItemId());
            ps.executeUpdate();
        }
    }

    /** Find request by ID. */
    public SubsidyRequest findById(String requestId) throws SQLException {
        String sql = "SELECT sr.*, f.full_name AS farmer_name, f.cnic AS farmer_cnic, c.name AS crop_name, it.name AS requested_item_name " +
                     "FROM subsidy_request sr " +
                     "JOIN farmer f ON sr.farmer_id = f.farmer_id " +
                     "JOIN crop c ON sr.crop_id = c.crop_id " +
                     "LEFT JOIN item_type it ON sr.requested_item_id = it.item_type_id " +
                     "WHERE sr.request_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, requestId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
            return null;
        }
    }

    /** Find all pending requests (UC-03). */
    public List<SubsidyRequest> findPending() throws SQLException {
        String sql = "SELECT sr.*, f.full_name AS farmer_name, f.cnic AS farmer_cnic, c.name AS crop_name, it.name AS requested_item_name " +
                     "FROM subsidy_request sr " +
                     "JOIN farmer f ON sr.farmer_id = f.farmer_id " +
                     "JOIN crop c ON sr.crop_id = c.crop_id " +
                     "LEFT JOIN item_type it ON sr.requested_item_id = it.item_type_id " +
                     "WHERE sr.status = 'PENDING' ORDER BY sr.request_date DESC";
        return executeQuery(sql);
    }

    /** Find all approved requests (UC-04). */
    public List<SubsidyRequest> findApproved() throws SQLException {
        String sql = "SELECT sr.*, f.full_name AS farmer_name, f.cnic AS farmer_cnic, c.name AS crop_name, it.name AS requested_item_name " +
                     "FROM subsidy_request sr " +
                     "JOIN farmer f ON sr.farmer_id = f.farmer_id " +
                     "JOIN crop c ON sr.crop_id = c.crop_id " +
                     "LEFT JOIN item_type it ON sr.requested_item_id = it.item_type_id " +
                     "WHERE sr.status = 'APPROVED' ORDER BY sr.request_date DESC";
        return executeQuery(sql);
    }

    /** Find requests by farmer ID. */
    public List<SubsidyRequest> findByFarmerId(String farmerId) throws SQLException {
        String sql = "SELECT sr.*, f.full_name AS farmer_name, f.cnic AS farmer_cnic, c.name AS crop_name, it.name AS requested_item_name " +
                     "FROM subsidy_request sr " +
                     "JOIN farmer f ON sr.farmer_id = f.farmer_id " +
                     "JOIN crop c ON sr.crop_id = c.crop_id " +
                     "LEFT JOIN item_type it ON sr.requested_item_id = it.item_type_id " +
                     "WHERE sr.farmer_id = ? ORDER BY sr.request_date DESC";
        List<SubsidyRequest> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, farmerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Check for duplicate pending/approved request for the same ITEM, crop, and season. */
    public boolean duplicateExists(String farmerId, String cropId, String season, String itemId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM subsidy_request WHERE farmer_id = ? AND crop_id = ? AND season = ? AND requested_item_id = ? " +
                     "AND status IN ('PENDING', 'APPROVED')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, farmerId);
            ps.setString(2, cropId);
            ps.setString(3, season);
            ps.setString(4, itemId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    /** Update request status and optional rejection reason. */
    public void updateStatus(String requestId, String status, String rejectionReason) throws SQLException {
        String sql = "UPDATE subsidy_request SET status = ?, rejection_reason = ? WHERE request_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, rejectionReason);
            ps.setString(3, requestId);
            ps.executeUpdate();
        }
    }

    /** Update the quantity (e.g., when an officer approves a reduced amount). */
    public void updateQuantity(String requestId, int quantity) throws SQLException {
        String sql = "UPDATE subsidy_request SET quantity = ? WHERE request_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setString(2, requestId);
            ps.executeUpdate();
        }
    }

    /** Update verification field. */
    public void updateVerification(String requestId, String verification) throws SQLException {
        String sql = "UPDATE subsidy_request SET verification = ? WHERE request_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, verification);
            ps.setString(2, requestId);
            ps.executeUpdate();
        }
    }

    /** Search requests by multiple filters (UC-11). */
    public List<SubsidyRequest> searchByFilters(String farmerName, String cnic,
            String cropId, String status, String date) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT sr.*, f.full_name AS farmer_name, f.cnic AS farmer_cnic, c.name AS crop_name, it.name AS requested_item_name " +
            "FROM subsidy_request sr " +
            "JOIN farmer f ON sr.farmer_id = f.farmer_id " +
            "JOIN crop c ON sr.crop_id = c.crop_id " +
            "LEFT JOIN item_type it ON sr.requested_item_id = it.item_type_id WHERE 1=1 ");
        List<String> params = new ArrayList<>();

        if (farmerName != null && !farmerName.isEmpty()) {
            sql.append("AND f.full_name LIKE ? ");
            params.add("%" + farmerName + "%");
        }
        if (cnic != null && !cnic.isEmpty()) {
            sql.append("AND f.cnic LIKE ? ");
            params.add("%" + cnic + "%");
        }
        if (cropId != null && !cropId.isEmpty()) {
            sql.append("AND sr.crop_id = ? ");
            params.add(cropId);
        }
        if (status != null && !status.isEmpty() && !status.equals("ALL")) {
            sql.append("AND sr.status = ? ");
            params.add(status);
        }
        if (date != null && !date.isEmpty()) {
            sql.append("AND CAST(sr.request_date AS DATE) = ? ");
            params.add(date);
        }
        sql.append("ORDER BY sr.request_date DESC");

        List<SubsidyRequest> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Search requests handled by a specific distributor. */
    public List<SubsidyRequest> searchByDistributor(String distributorId, String farmerName, String cnic) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT sr.*, f.full_name AS farmer_name, f.cnic AS farmer_cnic, c.name AS crop_name, it.name AS requested_item_name " +
            "FROM subsidy_request sr " +
            "JOIN farmer f ON sr.farmer_id = f.farmer_id " +
            "JOIN crop c ON sr.crop_id = c.crop_id " +
            "LEFT JOIN item_type it ON sr.requested_item_id = it.item_type_id " +
            "JOIN distribution_event de ON sr.request_id = de.request_id " +
            "WHERE de.distributor_id = ? ");
        
        List<String> params = new ArrayList<>();
        params.add(distributorId);

        if (farmerName != null && !farmerName.isEmpty()) {
            sql.append("AND f.full_name LIKE ? ");
            params.add("%" + farmerName + "%");
        }
        if (cnic != null && !cnic.isEmpty()) {
            sql.append("AND f.cnic LIKE ? ");
            params.add("%" + cnic + "%");
        }
        sql.append("ORDER BY sr.request_date DESC");

        List<SubsidyRequest> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Count requests by status. */
    public int countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM subsidy_request WHERE status = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Count requests by status and farmer ID. */
    public int countByStatusAndFarmerId(String status, String farmerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM subsidy_request WHERE status = ? AND farmer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, farmerId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Helper: execute a simple query and return list. */
    private List<SubsidyRequest> executeQuery(String sql) throws SQLException {
        List<SubsidyRequest> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Map a ResultSet row to a SubsidyRequest object. */
    private SubsidyRequest mapRow(ResultSet rs) throws SQLException {
        SubsidyRequest r = new SubsidyRequest();
        r.setRequestId(rs.getString("request_id"));
        r.setFarmerId(rs.getString("farmer_id"));
        r.setCropId(rs.getString("crop_id"));
        r.setStatus(rs.getString("status"));
        r.setQuantity(rs.getInt("quantity"));
        r.setRequestDate(rs.getTimestamp("request_date"));
        r.setSeason(rs.getString("season"));
        r.setRejectionReason(rs.getString("rejection_reason"));
        r.setVerification(rs.getString("verification"));
        r.setRequestedItemId(rs.getString("requested_item_id"));
        // JOINed fields
        try {
            r.setFarmerName(rs.getString("farmer_name"));
            r.setFarmerCnic(rs.getString("farmer_cnic"));
            r.setCropName(rs.getString("crop_name"));
            r.setRequestedItemName(rs.getString("requested_item_name"));
        } catch (SQLException ignored) {
            // These columns may not exist in all queries
        }
        return r;
    }
}
