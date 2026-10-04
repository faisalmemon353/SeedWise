package business.observer;

import business.model.InventoryStock;
import business.model.StockThreshold;
import data.StockAlertDAO;
import data.StockThresholdDAO;
import java.sql.SQLException;
import java.util.UUID;

/**
 * GoF Observer Concrete Observer.
 * Fires a low-stock alert to the DB when stock falls below threshold.
 * GRASP: Information Expert  knows thresholds, decides if alert needed.
 */
public class LowStockAlertObserver implements StockObserver {

    private StockThresholdDAO thresholdDAO;
    private StockAlertDAO alertDAO;

    public LowStockAlertObserver() throws SQLException {
        this.thresholdDAO = new StockThresholdDAO();
        this.alertDAO = new StockAlertDAO();
    }

    /**
     * Check if stock has fallen below threshold and create alert if so.
     * @param stock the updated inventory stock
     */
    @Override
    public void updateStock(InventoryStock stock) {
        try {
            StockThreshold threshold = thresholdDAO.findByItemTypeId(stock.getItemTypeId());
            if (threshold != null && stock.getQuantity() < threshold.getMinQuantity()) {
                if (!alertDAO.hasActiveAlert(stock.getItemTypeId())) {
                    // Persist low-stock alert
                    alertDAO.createAlert(UUID.randomUUID().toString(), stock.getItemTypeId());
                    System.out.println("[Observer] Low stock alert triggered for: " + stock.getItemTypeId());
                } else {
                    System.out.println("[Observer] Active low stock alert already exists for: " + stock.getItemTypeId());
                }
            }
        } catch (SQLException e) {
            System.err.println("[Observer] Error checking threshold: " + e.getMessage());
        }
    }
}
