package use_case.change_availability;

/** Results delivered to the presenter after this action. */
public interface ChangeAvailabilityOutputBoundary {
    void prepareSuccessView(ChangeAvailabilityOutputData outputData);
    void prepareFailView(String message);
}
