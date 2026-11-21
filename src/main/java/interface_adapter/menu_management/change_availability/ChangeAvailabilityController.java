package interface_adapter.menu_management.change_availability;

import use_case.menu_management.change_availability.ChangeAvailabilityInputBoundary;
import use_case.menu_management.change_availability.ChangeAvailabilityInputData;

/** Starts the ChangeAvailability use case with the current truck context. */
public final class ChangeAvailabilityController {
    private final ChangeAvailabilityInputBoundary inputBoundary;
    private final String truckId;
    public ChangeAvailabilityController(ChangeAvailabilityInputBoundary inputBoundary, String truckId) {
        this.inputBoundary = inputBoundary;
        this.truckId = truckId;
    }
    public void execute(String itemId, boolean available) {
        inputBoundary.execute(new ChangeAvailabilityInputData(truckId, itemId, available));
    }
}
