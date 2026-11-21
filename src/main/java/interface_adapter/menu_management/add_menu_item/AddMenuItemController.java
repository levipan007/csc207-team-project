package interface_adapter.menu_management.add_menu_item;

import use_case.menu_management.add_menu_item.AddMenuItemInputBoundary;
import use_case.menu_management.add_menu_item.AddMenuItemInputData;

/** Starts the AddMenuItem use case with the current truck context. */
public final class AddMenuItemController {
    private final AddMenuItemInputBoundary inputBoundary;
    private final String truckId;
    public AddMenuItemController(AddMenuItemInputBoundary inputBoundary, String truckId) {
        this.inputBoundary = inputBoundary;
        this.truckId = truckId;
    }
    public void execute(String name, String description, String price, String imageReference) {
        inputBoundary.execute(new AddMenuItemInputData(truckId, name, description, price, imageReference));
    }
}
