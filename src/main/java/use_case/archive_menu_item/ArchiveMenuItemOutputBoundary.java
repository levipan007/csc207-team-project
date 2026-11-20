package use_case.archive_menu_item;

/** Results delivered to the presenter after this action. */
public interface ArchiveMenuItemOutputBoundary {
    void prepareSuccessView(ArchiveMenuItemOutputData outputData);
    void prepareFailView(String message);
}
