package data;

import business.model.ItemType;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemTypeDAO {
    private final Connection conn;

    public ItemTypeDAO() throws SQLException {
        this.conn = DBConnection.getInstance().getConnection();
    }

    public ItemType findById(String itemTypeId) throws SQLException {
        String sql = "SELECT * FROM item_type WHERE item_type_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemTypeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ItemType it = new ItemType();
                it.setItemTypeId(rs.getString("item_type_id"));
                it.setName(rs.getString("name"));
                it.setUnit(rs.getString("unit"));
                it.setDescription(rs.getString("description"));
                return it;
            }
            return null;
        }
    }
    public List<ItemType> findAll() throws SQLException {
        List<ItemType> list = new ArrayList<>();
        String sql = "SELECT * FROM item_type ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ItemType it = new ItemType();
                it.setItemTypeId(rs.getString("item_type_id"));
                it.setName(rs.getString("name"));
                it.setUnit(rs.getString("unit"));
                it.setDescription(rs.getString("description"));
                list.add(it);
            }
        }
        return list;
    }
}
