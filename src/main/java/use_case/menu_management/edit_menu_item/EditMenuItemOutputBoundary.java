package use_case.menu_management.edit_menu_item;

/** Results delivered to the presenter after this action. */
public interface EditMenuItemOutputBoundary {
    void prepareSuccessView(EditMenuItemOutputData outputData);
    void prepareFailView(String message);
}
