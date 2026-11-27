package use_case.menu_management.change_availability;

import entity.MenuItem;
import java.util.Objects;
import use_case.shared.DataAccessException;
import use_case.menu_management.menu.MenuItemData;
import use_case.menu_management.menu.MenuValidation;

/** Validates the truck and stores an immutable replacement for an existing item. */
public final class ChangeAvailabilityInteractor implements ChangeAvailabilityInputBoundary {
    private final ChangeAvailabilityDataAccessInterface dataAccess;
    private final ChangeAvailabilityOutputBoundary presenter;

    public ChangeAvailabilityInteractor(ChangeAvailabilityDataAccessInterface dataAccess,
                            ChangeAvailabilityOutputBoundary presenter) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess");
        this.presenter = Objects.requireNonNull(presenter, "presenter");
    }

    @Override
    public void execute(ChangeAvailabilityInputData inputData) {
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
        final MenuItem updated = item.withAvailability(inputData.isAvailable());
        try {
            dataAccess.save(updated);
        }
        catch (DataAccessException exception) {
            presenter.prepareFailView("Could not save the menu item. Please try again.");
            return;
        }
        presenter.prepareSuccessView(new ChangeAvailabilityOutputData(new MenuItemData(updated)));
    }
}
