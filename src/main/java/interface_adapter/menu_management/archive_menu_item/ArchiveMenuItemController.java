package interface_adapter.menu_management.archive_menu_item;

import interface_adapter.session.CurrentSession;
import use_case.menu_management.archive_menu_item.ArchiveMenuItemInputBoundary;
import use_case.menu_management.archive_menu_item.ArchiveMenuItemInputData;
import java.util.Objects;

/** Starts the ArchiveMenuItem use case with the truck in the current session. */
public final class ArchiveMenuItemController {
    private final ArchiveMenuItemInputBoundary inputBoundary;
    private final CurrentSession session;

    public ArchiveMenuItemController(ArchiveMenuItemInputBoundary inputBoundary, CurrentSession session) {
        this.inputBoundary = Objects.requireNonNull(inputBoundary);
        this.session = Objects.requireNonNull(session);
    }

    public void execute(String itemId) {
        inputBoundary.execute(new ArchiveMenuItemInputData(session.getTruckId(), itemId));
    }
}
