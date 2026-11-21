package use_case.menu_management.archive_menu_item;

/** Values supplied when the user requests this action. */
public final class ArchiveMenuItemInputData {
    private final String truckId;
    private final String itemId;

    public ArchiveMenuItemInputData(String truckId, String itemId) {
        this.truckId = truckId;
        this.itemId = itemId;
    }

    public String getTruckId() { return truckId; }
    public String getItemId() { return itemId; }
}
