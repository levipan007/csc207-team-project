package use_case.view_menu;

/** Values supplied when the user requests this action. */
public final class ViewMenuInputData {
    private final String truckId;

    public ViewMenuInputData(String truckId) {
        this.truckId = truckId;
    }

    public String getTruckId() { return truckId; }
}
