package interface_adapter.menu_management.edit_menu_item;

import interface_adapter.session.CurrentSession;
import use_case.menu_management.edit_menu_item.EditMenuItemInputBoundary;
import use_case.menu_management.edit_menu_item.EditMenuItemInputData;
import java.util.Objects;

/** Starts the EditMenuItem use case with the truck in the current session. */
public final class EditMenuItemController {
    private final EditMenuItemInputBoundary inputBoundary;
    private final CurrentSession session;

    public EditMenuItemController(EditMenuItemInputBoundary inputBoundary, CurrentSession session) {
        this.inputBoundary = Objects.requireNonNull(inputBoundary);
        this.session = Objects.requireNonNull(session);
    }

    public void execute(String itemId, String name, String description, String price, String imageReference) {
        inputBoundary.execute(new EditMenuItemInputData(session.getTruckId(), itemId, name, description, price, imageReference));
    }
}
