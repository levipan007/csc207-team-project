package use_case.menu_management.edit_menu_item;

import entity.MenuItem;
import java.util.Objects;
import use_case.shared.DataAccessException;
import use_case.menu_management.menu.MenuItemData;
import use_case.menu_management.menu.MenuValidation;

/** Validates the truck and stores an immutable replacement for an existing item. */
public final class EditMenuItemInteractor implements EditMenuItemInputBoundary {
    private final EditMenuItemDataAccessInterface dataAccess;
    private final EditMenuItemOutputBoundary presenter;

    public EditMenuItemInteractor(EditMenuItemDataAccessInterface dataAccess,
                            EditMenuItemOutputBoundary presenter) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess");
        this.presenter = Objects.requireNonNull(presenter, "presenter");
    }

    @Override
    public void execute(EditMenuItemInputData inputData) {
        if (inputData == null) {
            presenter.prepareFailView("Menu item details are required.");
            return;
        }
        final String truckId;
        final String itemId;
        try {
            truckId = MenuValidation.requireText(inputData.getTruckId(), "Truck ID");
            itemId = MenuValidation.requireText(inputData.getItemId(), "Item ID");
        }
        catch (IllegalArgumentException exception) {
            presenter.prepareFailView(exception.getMessage());
            return;
        }
        final MenuItem item;
        try {
            item = dataAccess.get(itemId);
        }
        catch (DataAccessException exception) {
            presenter.prepareFailView("Could not load the menu item. Please try again.");
            return;
        }
        if (item == null || !truckId.equals(item.getTruckId())) {
            presenter.prepareFailView("Menu item was not found for this truck.");
            return;
        }
        if (item.isArchived()) {
            presenter.prepareFailView("Archived menu items cannot be changed.");
            return;
        }
        final MenuItem updated;
        try {
            updated = item.withDetails(inputData.getName(), inputData.getDescription(),
                    MenuValidation.parsePriceCents(inputData.getPrice()), inputData.getImageReference());
        }
        catch (IllegalArgumentException exception) {
            presenter.prepareFailView(exception.getMessage());
            return;
        }
        try {
            dataAccess.save(updated);
        }
        catch (DataAccessException exception) {
            presenter.prepareFailView("Could not save the menu item. Please try again.");
            return;
        }
        presenter.prepareSuccessView(new EditMenuItemOutputData(new MenuItemData(updated)));
    }
}
