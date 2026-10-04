package business.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import business.observer.StockObserver;

/**
 * Domain model for InventoryStock entity.
 * GoF Observer Subject - notifies observers when stock changes.
 * GRASP: Information Expert - knows its own quantity and handles stock math.
 */
public class InventoryStock {
    private String stockId;
    private String itemTypeId;
    private int quantity;
    private Date lastUpdated;
    private List<StockObserver> observers;  // GoF Observer pattern

    // Display fields (from JOINs)
    private String itemName;
    private String unit;

    // --- Constructors ---

    /** Default constructor. */
    public InventoryStock() {
        this.observers = new ArrayList<>();
    }

    // --- GoF Observer methods ---

    /** Register a new observer. */
    public void addObserver(StockObserver o) {
        observers.add(o);
    }

    /** Remove an observer. */
    public void removeObserver(StockObserver o) {
        observers.remove(o);
    }

    /** Notify all registered observers (used when stock drops below threshold). */
    private void notifyObservers() {
        for (StockObserver o : observers) {
            o.updateStock(this);
        }
    }

    // --- Stock manipulation ---

    /** Add stock and notify observers. */
    public void addStock(int qty) {
        this.quantity += qty;
        this.lastUpdated = new Date();
        notifyObservers();
    }

    /** Subtract stock and notify observers. */
    public void subtractStock(int qty) {
        this.quantity -= qty;
        this.lastUpdated = new Date();
        notifyObservers();
    }

    // --- Getters and Setters ---

    public String getStockId() { return stockId; }
    public void setStockId(String stockId) { this.stockId = stockId; }

    public String getItemTypeId() { return itemTypeId; }
    public void setItemTypeId(String itemTypeId) { this.itemTypeId = itemTypeId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Date getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
