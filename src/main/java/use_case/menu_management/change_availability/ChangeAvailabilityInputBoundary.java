package use_case.menu_management.change_availability;

/** The operation exposed to this use case's controller. */
public interface ChangeAvailabilityInputBoundary {
    void execute(ChangeAvailabilityInputData inputData);
}
