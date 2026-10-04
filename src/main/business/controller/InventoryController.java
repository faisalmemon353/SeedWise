package business.controller;

import business.model.*;
import business.observer.LowStockAlertObserver;
import business.singleton.AuditLogger;
import data.*;
import java.sql.SQLException;
import java.util.*;

/**
 * GRASP Controller - InventoryController.
 * Handles all inventory-related use case events.
 * 
 * UC-04: recordDistribution()
 * UC-05: addInventoryStock()
 * UC-09: getActiveAlerts() / acknowledgeAlert()
 * UC-11: searchRequests()
 * UC-12: configureThreshold()
 */
public class InventoryController {

    private final InventoryDAO inventoryDAO;
    private final DistributionDAO distributionDAO;
    private final SubsidyRequestDAO requestDAO;
    private final StockAlertDAO alertDAO;
    private final StockThresholdDAO thresholdDAO;
    private final LowStockAlertObserver stockObserver;
    private final AuditLogger auditLogger;

    public InventoryController() throws SQLException {
        this.inventoryDAO = new InventoryDAO();
        this.distributionDAO = new DistributionDAO();
        this.requestDAO = new SubsidyRequestDAO();
        this.alertDAO = new StockAlertDAO();
        this.thresholdDAO = new StockThresholdDAO();
        this.stockObserver = new LowStockAlertObserver();
        this.auditLogger = AuditLogger.getInstance();
    }

    // UC-04: DISTRIBUTE SUPPLIES
    /**
     * Records a distribution event and reduces inventory stock.
     * Triggers Observer chain via stock subtraction.
     * GRASP: Controller coordinates DistributionDAO, InventoryDAO,
     * SubsidyRequestDAO.
     */
    public String recordDistribution(String requestId, String distributorId,
            int quantity, String itemTypeId) throws SQLException {
        // Verify request is APPROVED
        SubsidyRequest request = requestDAO.findById(requestId);
        if (request == null)
            throw new IllegalStateException("Request not found");
        if (!"APPROVED".equals(request.getStatus())) {
            throw new IllegalStateException("Request is not in APPROVED status. Current: " + request.getStatus());
        }

        // Check stock sufficiency
        int currentQty = inventoryDAO.getStockQuantity(itemTypeId);
        if (currentQty < quantity) {
            throw new IllegalStateException(
                    "Insufficient stock. Available: " + currentQty + ", Requested: " + quantity);
        }

        // Record distribution event
        String distId = UUID.randomUUID().toString();
        DistributionEvent event = new DistributionEvent(distId, requestId, distributorId, quantity);
        distributionDAO.insert(event);

        // Subtract stock
        inventoryDAO.subtractStock(itemTypeId, quantity);

        // GoF Observer - check if stock fell below threshold
        InventoryStock updatedStock = inventoryDAO.findStockByItemTypeId(itemTypeId);
        if (updatedStock != null) {
            stockObserver.updateStock(updatedStock);
        }

        // Update request status to DISTRIBUTED
        requestDAO.updateStatus(requestId, "DISTRIBUTED", null);

        // Audit log
        auditLogger.logAction(requestId, "DISTRIBUTE_SUPPLIES", distributorId, "DISTRIBUTOR",
                "Distributed qty=" + quantity + " for item=" + itemTypeId);

        return distId;
    }

    // UC-05: ADD INVENTORY STOCK
    /**
     * Adds stock to inventory.
     * GRASP: Information Expert knows how to validate and persist stock changes.
     */
    public InventoryStock addInventoryStock(String itemTypeId, int quantity, String actorId) throws SQLException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive number.");
        }
        inventoryDAO.addStock(itemTypeId, quantity);
        auditLogger.logAction(itemTypeId, "ADD_STOCK", actorId, "DISTRIBUTOR", "Added qty=" + quantity);
        return inventoryDAO.findStockByItemTypeId(itemTypeId);
    }

    // UC-09: MONITOR STOCK ALERTS
    /** Returns all active low-stock alerts. */
    public List<StockAlert> getActiveAlerts() throws SQLException {
        return alertDAO.findActive();
    }

    /** Acknowledge an alert with a note. */
    public void acknowledgeAlert(String alertId, String note, String actorId) throws SQLException {
        alertDAO.acknowledge(alertId, note);
        auditLogger.logAction(alertId, "ACKNOWLEDGE_ALERT", actorId, "DISTRIBUTOR", "Note: " + note);
    }

    // UC-11: SEARCH REQUESTS
    /** Search subsidy requests by multiple filters. */
    public List<SubsidyRequest> searchRequests(String farmerName, String cnic,
            String cropId, String status, String date) throws SQLException {
        return requestDAO.searchByFilters(farmerName, cnic, cropId, status, date);
    }

    // UC-12: CONFIGURE THRESHOLD
    /**
     * Sets or updates the minimum stock threshold for an item type.
     * Immediately checks current stock against new threshold.
     */
    public String configureThreshold(String itemTypeId, int minQuantity, String actorId) throws SQLException {
        if (minQuantity < 0) {
            throw new IllegalArgumentException("Threshold must be a non-negative number.");
        }
        String thresholdId = UUID.randomUUID().toString();
        StockThreshold threshold = new StockThreshold(thresholdId, itemTypeId, minQuantity);
        thresholdDAO.upsert(threshold);

        // Check if current stock is below new threshold
        int currentQty = inventoryDAO.getStockQuantity(itemTypeId);
        String warning = null;
        if (currentQty < minQuantity) {
            warning = "Warning: Current stock (" + currentQty + ") is below the new threshold (" + minQuantity + ").";
            InventoryStock stock = inventoryDAO.findStockByItemTypeId(itemTypeId);
            if (stock == null) {
                stock = new InventoryStock();
                stock.setItemTypeId(itemTypeId);
                stock.setQuantity(currentQty);
            }
            stockObserver.updateStock(stock);
        }

        auditLogger.logAction(itemTypeId, "CONFIGURE_THRESHOLD", actorId, "DISTRIBUTOR",
                "Set threshold=" + minQuantity);
        return warning;
    }

    // Helper methods
    public List<InventoryStock> getAllStock() throws SQLException {
        return inventoryDAO.findAllStock();
    }

    public List<ItemType> getAllItemTypes() throws SQLException {
        return inventoryDAO.findAllItemTypes();
    }

    public List<StockThreshold> getAllThresholds() throws SQLException {
        return thresholdDAO.findAll();
    }
}
