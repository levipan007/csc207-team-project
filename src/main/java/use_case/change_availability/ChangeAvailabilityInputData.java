package use_case.change_availability;

/** Values supplied when the user requests this action. */
public final class ChangeAvailabilityInputData {
    private final String truckId;
    private final String itemId;
    private final boolean available;

    public ChangeAvailabilityInputData(String truckId, String itemId, boolean available) {
        this.truckId = truckId;
        this.itemId = itemId;
        this.available = available;
    }

    public String getTruckId() { return truckId; }
    public String getItemId() { return itemId; }
    public boolean isAvailable() { return available; }
}
