package interface_adapter.menu_management.archive_menu_item;

import use_case.menu_management.archive_menu_item.ArchiveMenuItemInputBoundary;
import use_case.menu_management.archive_menu_item.ArchiveMenuItemInputData;

/** Starts the ArchiveMenuItem use case with the current truck context. */
public final class ArchiveMenuItemController {
    private final ArchiveMenuItemInputBoundary inputBoundary;
    private final String truckId;
    public ArchiveMenuItemController(ArchiveMenuItemInputBoundary inputBoundary, String truckId) {
        this.inputBoundary = inputBoundary;
        this.truckId = truckId;
    }
    public void execute(String itemId) {
        inputBoundary.execute(new ArchiveMenuItemInputData(truckId, itemId));
    }
}
