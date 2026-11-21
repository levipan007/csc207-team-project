package use_case.menu_management.view_sales_summary;

/** Sales totals for one menu item, retaining its most recent purchase name. */
public final class TopItemData {
    private final String itemId;
    private final String name;
    private final long quantity;
    private final long revenueCents;

    public TopItemData(String itemId, String name, long quantity, long revenueCents) {
        this.itemId = itemId;
        this.name = name;
        this.quantity = quantity;
        this.revenueCents = revenueCents;
    }

    public String getItemId() { return itemId; }
    public String getName() { return name; }
    public long getQuantity() { return quantity; }
    public long getRevenueCents() { return revenueCents; }
}
