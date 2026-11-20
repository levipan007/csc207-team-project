package entity;

/** A purchase-time snapshot; later menu edits do not change past sales. */
public final class SaleLine {
    private final String itemId;
    private final String nameAtPurchase;
    private final long unitPriceCents;
    private final int quantity;

    public SaleLine(String itemId, String nameAtPurchase, long unitPriceCents, int quantity) {
        if (itemId == null || itemId.trim().isEmpty()
                || nameAtPurchase == null || nameAtPurchase.trim().isEmpty()) {
            throw new IllegalArgumentException("Sale item ID and purchase name are required.");
        }
        if (unitPriceCents < 0 || quantity <= 0) {
            throw new IllegalArgumentException("Sale price cannot be negative and quantity must be positive.");
        }
        Math.multiplyExact(unitPriceCents, (long) quantity);
        this.itemId = itemId;
        this.nameAtPurchase = nameAtPurchase;
        this.unitPriceCents = unitPriceCents;
        this.quantity = quantity;
    }

    public String getItemId() { return itemId; }
    public String getNameAtPurchase() { return nameAtPurchase; }
    public long getUnitPriceCents() { return unitPriceCents; }
    public int getQuantity() { return quantity; }
    public long getLineTotalCents() { return Math.multiplyExact(unitPriceCents, (long) quantity); }
}
