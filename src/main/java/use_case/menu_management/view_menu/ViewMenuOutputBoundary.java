package use_case.menu_management.view_menu;

/** Results delivered to the presenter after this action. */
public interface ViewMenuOutputBoundary {
    void prepareSuccessView(ViewMenuOutputData outputData);
    void prepareFailView(String message);
}
