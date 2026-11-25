package entity;

/** One immutable line of an order. Name and price are copied from the menu at order time. */
public final class OrderItem {
    private final String menuItemId;
    private final String nameAtOrder;
    private final long unitPriceCents;
    private final int quantity;
    private final String note;

    /** Creates a line. Uses the same rules as {@link SaleLine}, so a finished order always converts. */
    public OrderItem(String menuItemId, String nameAtOrder, long unitPriceCents, int quantity, String note) {
        if (menuItemId == null || menuItemId.trim().isEmpty()
                || nameAtOrder == null || nameAtOrder.trim().isEmpty()) {
            throw new IllegalArgumentException("Each order line needs a menu item ID and name.");
        }
        if (unitPriceCents < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        Math.multiplyExact(unitPriceCents, (long) quantity);
        this.menuItemId = menuItemId.trim();
        this.nameAtOrder = nameAtOrder.trim();
        this.unitPriceCents = unitPriceCents;
        this.quantity = quantity;
        this.note = note == null ? "" : note.trim();
    }

    public String getMenuItemId() { return menuItemId; }
    public String getNameAtOrder() { return nameAtOrder; }
    public long getUnitPriceCents() { return unitPriceCents; }
    public int getQuantity() { return quantity; }
    public String getNote() { return note; }

    /** Returns the price of this line, in cents. */
    public long subtotalCents() {
        return Math.multiplyExact(unitPriceCents, (long) quantity);
    }

    /** Returns this line with a different quantity. */
    public OrderItem withQuantity(int newQuantity) {
        return new OrderItem(menuItemId, nameAtOrder, unitPriceCents, newQuantity, note);
    }

    /** Returns this line with a different note. */
    public OrderItem withNote(String newNote) {
        return new OrderItem(menuItemId, nameAtOrder, unitPriceCents, quantity, newNote);
    }

    /** True when both lines are the same menu item with the same note, so they can be merged. */
    boolean sameLineAs(OrderItem other) {
        return menuItemId.equals(other.menuItemId) && note.equals(other.note);
    }

    SaleLine toSaleLine() {
        return new SaleLine(menuItemId, nameAtOrder, unitPriceCents, quantity);
    }
}
