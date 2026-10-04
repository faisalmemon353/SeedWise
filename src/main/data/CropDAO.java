package data;

import business.model.Crop;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Crop entity.
 * GRASP: Low Coupling - UI and BL never touch SQL directly.
 */
public class CropDAO {

    private final Connection conn;

    public CropDAO() throws SQLException {
        this.conn = DBConnection.getInstance().getConnection();
    }

    /** Find all crops. */
    public List<Crop> findAll() throws SQLException {
        String sql = "SELECT * FROM crop ORDER BY name";
        List<Crop> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Crop(
                    rs.getString("crop_id"),
                    rs.getString("name"),
                    rs.getString("description")
                ));
            }
        }
        return list;
    }

    /** Find crop by ID. */
    public Crop findById(String cropId) throws SQLException {
        String sql = "SELECT * FROM crop WHERE crop_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cropId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Crop(
                    rs.getString("crop_id"),
                    rs.getString("name"),
                    rs.getString("description")
                );
            }
            return null;
        }
    }
}
