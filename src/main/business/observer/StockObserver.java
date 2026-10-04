package business.observer;

import business.model.InventoryStock;

/**
 * GoF Observer interface.
 * Implemented by any component needing to react to stock level changes.
 * OOP: Abstraction â€” defines contract without implementation.
 */
public interface StockObserver {
    /**
     * Called when stock levels change.
     * @param stock the updated inventory stock
     */
    void updateStock(InventoryStock stock);
}
