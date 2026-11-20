package interface_adapter.edit_menu_item;

import use_case.edit_menu_item.EditMenuItemInputBoundary;
import use_case.edit_menu_item.EditMenuItemInputData;

/** Starts the EditMenuItem use case with the current truck context. */
public final class EditMenuItemController {
    private final EditMenuItemInputBoundary inputBoundary;
    private final String truckId;
    public EditMenuItemController(EditMenuItemInputBoundary inputBoundary, String truckId) {
        this.inputBoundary = inputBoundary;
        this.truckId = truckId;
    }
    public void execute(String itemId, String name, String description, String price, String imageReference) {
        inputBoundary.execute(new EditMenuItemInputData(truckId, itemId, name, description, price, imageReference));
    }
}
