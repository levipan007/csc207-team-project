package use_case.menu_management.add_menu_item;

/** Results delivered to the presenter after this action. */
public interface AddMenuItemOutputBoundary {
    void prepareSuccessView(AddMenuItemOutputData outputData);
    void prepareFailView(String message);
}
