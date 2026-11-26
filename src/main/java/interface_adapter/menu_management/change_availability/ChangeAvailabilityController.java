package interface_adapter.menu_management.change_availability;

import interface_adapter.session.CurrentSession;
import use_case.menu_management.change_availability.ChangeAvailabilityInputBoundary;
import use_case.menu_management.change_availability.ChangeAvailabilityInputData;
import java.util.Objects;

/** Starts the ChangeAvailability use case with the truck in the current session. */
public final class ChangeAvailabilityController {
    private final ChangeAvailabilityInputBoundary inputBoundary;
    private final CurrentSession session;

    public ChangeAvailabilityController(ChangeAvailabilityInputBoundary inputBoundary, CurrentSession session) {
        this.inputBoundary = Objects.requireNonNull(inputBoundary);
        this.session = Objects.requireNonNull(session);
    }

    public void execute(String itemId, boolean available) {
        inputBoundary.execute(new ChangeAvailabilityInputData(session.getTruckId(), itemId, available));
    }
}
