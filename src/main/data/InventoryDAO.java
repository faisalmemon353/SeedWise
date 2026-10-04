package data;

import business.model.InventoryStock;
import business.model.ItemType;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for InventoryStock and ItemType entities.
 * GRASP: Low Coupling â€” UI and BL never touch SQL directly.
 */
public class InventoryDAO {

    private final Connection conn;

    public InventoryDAO() throws SQLException {
        this.conn = DBConnection.getInstance().getConnection();
    }

    /** Find all item types. */
    public List<ItemType> findAllItemTypes() throws SQLException {
        String sql = "SELECT * FROM item_type ORDER BY name";
        List<ItemType> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ItemType(
                    rs.getString("item_type_id"),
                    rs.getString("name"),
                    rs.getString("unit"),
                    rs.getString("description")
                ));
            }
        }
        return list;
    }

    /** Find all stock with item names. */
    public List<InventoryStock> findAllStock() throws SQLException {
        String sql = "SELECT ist.*, it.name AS item_name, it.unit FROM inventory_stock ist " +
                     "JOIN item_type it ON ist.item_type_id = it.item_type_id ORDER BY it.name";
        List<InventoryStock> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapStockRow(rs));
            }
        }
        return list;
    }

    /** Find stock by item type ID. */
    public InventoryStock findStockByItemTypeId(String itemTypeId) throws SQLException {
        String sql = "SELECT ist.*, it.name AS item_name, it.unit FROM inventory_stock ist " +
                     "JOIN item_type it ON ist.item_type_id = it.item_type_id " +
                     "WHERE ist.item_type_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemTypeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapStockRow(rs);
            return null;
        }
    }

    /** Get stock quantity for an item type. */
    public int getStockQuantity(String itemTypeId) throws SQLException {
        String sql = "SELECT quantity FROM inventory_stock WHERE item_type_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemTypeId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("quantity") : 0;
        }
    }

    public void addStock(String itemTypeId, int quantity) throws SQLException {
        String checkSql = "SELECT stock_id FROM inventory_stock WHERE item_type_id = ?";
        boolean exists = false;
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, itemTypeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) exists = true;
        }

        if (exists) {
            String sql = "UPDATE inventory_stock SET quantity = quantity + ?, last_updated = GETDATE() WHERE item_type_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, quantity);
                ps.setString(2, itemTypeId);
                ps.executeUpdate();
            }
        } else {
            String sql = "INSERT INTO inventory_stock (stock_id, item_type_id, quantity, last_updated) VALUES (?, ?, ?, GETDATE())";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, java.util.UUID.randomUUID().toString());
                ps.setString(2, itemTypeId);
                ps.setInt(3, quantity);
                ps.executeUpdate();
            }
        }
    }

    /** Subtract stock. */
    public void subtractStock(String itemTypeId, int quantity) throws SQLException {
        String sql = "UPDATE inventory_stock SET quantity = quantity - ?, last_updated = GETDATE() WHERE item_type_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setString(2, itemTypeId);
            ps.executeUpdate();
        }
    }

    /** Map ResultSet row to InventoryStock. */
    private InventoryStock mapStockRow(ResultSet rs) throws SQLException {
        InventoryStock s = new InventoryStock();
        s.setStockId(rs.getString("stock_id"));
        s.setItemTypeId(rs.getString("item_type_id"));
        s.setQuantity(rs.getInt("quantity"));
        s.setLastUpdated(rs.getTimestamp("last_updated"));
        s.setItemName(rs.getString("item_name"));
        s.setUnit(rs.getString("unit"));
        return s;
    }
}
