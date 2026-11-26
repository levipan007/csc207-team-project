package interface_adapter.menu_management.add_menu_item;

import interface_adapter.session.CurrentSession;
import use_case.menu_management.add_menu_item.AddMenuItemInputBoundary;
import use_case.menu_management.add_menu_item.AddMenuItemInputData;
import java.util.Objects;

/** Starts the AddMenuItem use case with the truck in the current session. */
public final class AddMenuItemController {
    private final AddMenuItemInputBoundary inputBoundary;
    private final CurrentSession session;

    public AddMenuItemController(AddMenuItemInputBoundary inputBoundary, CurrentSession session) {
        this.inputBoundary = Objects.requireNonNull(inputBoundary);
        this.session = Objects.requireNonNull(session);
    }

    public void execute(String name, String description, String price, String imageReference) {
        inputBoundary.execute(new AddMenuItemInputData(session.getTruckId(), name, description, price, imageReference));
    }
}
