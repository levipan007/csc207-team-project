package use_case.menu_management.change_availability;

/** Results delivered to the presenter after this action. */
public interface ChangeAvailabilityOutputBoundary {
    void prepareSuccessView(ChangeAvailabilityOutputData outputData);
    void prepareFailView(String message);
}
