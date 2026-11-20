package use_case.change_availability;

/** The operation exposed to this use case's controller. */
public interface ChangeAvailabilityInputBoundary {
    void execute(ChangeAvailabilityInputData inputData);
}
