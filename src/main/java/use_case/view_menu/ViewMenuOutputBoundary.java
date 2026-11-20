package use_case.view_menu;

/** Results delivered to the presenter after this action. */
public interface ViewMenuOutputBoundary {
    void prepareSuccessView(ViewMenuOutputData outputData);
    void prepareFailView(String message);
}
