package use_case.archive_menu_item;

import entity.MenuItem;
import java.util.Objects;
import use_case.menu.DataAccessException;
import use_case.menu.MenuItemData;
import use_case.menu.MenuValidation;

/** Validates the truck and stores an immutable replacement for an existing item. */
public final class ArchiveMenuItemInteractor implements ArchiveMenuItemInputBoundary {
    private final ArchiveMenuItemDataAccessInterface dataAccess;
    private final ArchiveMenuItemOutputBoundary presenter;

    public ArchiveMenuItemInteractor(ArchiveMenuItemDataAccessInterface dataAccess,
                            ArchiveMenuItemOutputBoundary presenter) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess");
        this.presenter = Objects.requireNonNull(presenter, "presenter");
    }

    @Override
    public void execute(ArchiveMenuItemInputData inputData) {
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
        final MenuItem updated = item.archive();
        try {
            dataAccess.save(updated);
        }
        catch (DataAccessException exception) {
            presenter.prepareFailView("Could not save the menu item. Please try again.");
            return;
        }
        presenter.prepareSuccessView(new ArchiveMenuItemOutputData(new MenuItemData(updated)));
    }
}
