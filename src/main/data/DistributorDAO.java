package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import business.model.Distributor;

public class DistributorDAO {
    private final Connection conn;

    public DistributorDAO() throws SQLException {
        this.conn = DBConnection.getInstance().getConnection();
    }

    public void insert(String distributorId, String userId, String name, String contact, String cnic, String address) throws SQLException {
        String sql = "INSERT INTO distributor (distributor_id, user_id, name, contact, cnic, address) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, distributorId);
            ps.setString(2, userId);
            ps.setString(3, name);
            ps.setString(4, contact);
            ps.setString(5, cnic);
            ps.setString(6, address);
            ps.executeUpdate();
        }
    }

    public List<Distributor> getAllDistributors() throws SQLException {
        List<Distributor> list = new ArrayList<>();
        String sql = "SELECT * FROM distributor WHERE status = 'ACTIVE' OR status IS NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Distributor(
                    rs.getString("distributor_id"),
                    rs.getString("user_id"),
                    rs.getString("name"),
                    rs.getString("contact"),
                    rs.getString("cnic"),
                    rs.getString("address")
                ));
            }
        }
        return list;
    }

    public String findByUserId(String userId) throws SQLException {
        String sql = "SELECT distributor_id FROM distributor WHERE user_id = ? AND (status = 'ACTIVE' OR status IS NULL)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("distributor_id");
            }
        }
        return null;
    }

    public void deleteByUserId(String userId) throws SQLException {
        String sql = "UPDATE distributor SET status = 'INACTIVE' WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        }
    }
}
