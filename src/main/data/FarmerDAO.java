package data;

import business.model.Farmer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object for Farmer entity.
 * GRASP: Low Coupling - UI and BL never touch SQL directly.
 */
public class FarmerDAO {

    private final Connection conn;

    public FarmerDAO() throws SQLException {
        this.conn = DBConnection.getInstance().getConnection();
    }

    /** Insert a new Farmer record. Returns generated farmer_id. */
    public String insertFarmer(Farmer farmer) throws SQLException {
        String sql = "INSERT INTO farmer (farmer_id, user_id, full_name, cnic, contact, address, status, remaining_allocation) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String farmerId = farmer.getFarmerId() != null ? farmer.getFarmerId() : UUID.randomUUID().toString();
            ps.setString(1, farmerId);
            ps.setString(2, farmer.getUserId());
            ps.setString(3, farmer.getFullName());
            ps.setString(4, farmer.getCnic());
            ps.setString(5, farmer.getContact());
            ps.setString(6, farmer.getAddress());
            ps.setString(7, farmer.getStatus());
            ps.setInt(8, farmer.getRemainingAllocation());
            ps.executeUpdate();
            return farmerId;
        }
    }

    /** Check if a CNIC already exists. */
    public boolean cnicExists(String cnic) throws SQLException {
        String sql = "SELECT COUNT(*) FROM farmer WHERE cnic = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cnic);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    /** Find farmer by user ID. */
    public Farmer findByUserId(String userId) throws SQLException {
        String sql = "SELECT * FROM farmer WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
            return null;
        }
    }

    /** Find farmer by farmer ID. */
    public Farmer findByFarmerId(String farmerId) throws SQLException {
        String sql = "SELECT * FROM farmer WHERE farmer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, farmerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
            return null;
        }
    }

    /** Find farmer by CNIC. */
    public Farmer findByCnic(String cnic) throws SQLException {
        String sql = "SELECT * FROM farmer WHERE cnic = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cnic);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
            return null;
        }
    }

    /** Update remaining allocation for a farmer. */
    public void updateRemainingAllocation(String farmerId, int newAllocation) throws SQLException {
        String sql = "UPDATE farmer SET remaining_allocation = ? WHERE farmer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newAllocation);
            ps.setString(2, farmerId);
            ps.executeUpdate();
        }
    }

    /** Update farmer status. */
    public void updateStatus(String farmerId, String status) throws SQLException {
        String sql = "UPDATE farmer SET status = ? WHERE farmer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, farmerId);
            ps.executeUpdate();
        }
    }

    /** Find all farmers. */
    public List<Farmer> findAll() throws SQLException {
        String sql = "SELECT * FROM farmer ORDER BY full_name";
        List<Farmer> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Map a ResultSet row to a Farmer object. */
    private Farmer mapRow(ResultSet rs) throws SQLException {
        Farmer f = new Farmer();
        f.setFarmerId(rs.getString("farmer_id"));
        f.setUserId(rs.getString("user_id"));
        f.setFullName(rs.getString("full_name"));
        f.setCnic(rs.getString("cnic"));
        f.setContact(rs.getString("contact"));
        f.setAddress(rs.getString("address"));
        f.setStatus(rs.getString("status"));
        f.setRemainingAllocation(rs.getInt("remaining_allocation"));
        return f;
    }
}
