package use_case.menu_management.add_menu_item;

import entity.MenuItem;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import use_case.menu_management.menu.DataAccessException;
import use_case.menu_management.menu.MenuItemData;
import use_case.menu_management.menu.MenuValidation;

/** Creates and stores one menu item after validating the owner's request. */
public final class AddMenuItemInteractor implements AddMenuItemInputBoundary {
    private final AddMenuItemDataAccessInterface dataAccess;
    private final AddMenuItemOutputBoundary presenter;
    private final Supplier<String> idSupplier;

    public AddMenuItemInteractor(AddMenuItemDataAccessInterface dataAccess,
                                 AddMenuItemOutputBoundary presenter) {
        this(dataAccess, presenter, () -> UUID.randomUUID().toString());
    }

    public AddMenuItemInteractor(AddMenuItemDataAccessInterface dataAccess,
                                 AddMenuItemOutputBoundary presenter, Supplier<String> idSupplier) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess");
        this.presenter = Objects.requireNonNull(presenter, "presenter");
        this.idSupplier = Objects.requireNonNull(idSupplier, "idSupplier");
    }

    @Override
    public void execute(AddMenuItemInputData inputData) {
        if (inputData == null) {
            presenter.prepareFailView("Menu item details are required.");
            return;
        }
        final MenuItem item;
        try {
            long cents = MenuValidation.parsePriceCents(inputData.getPrice());
            item = new MenuItem(idSupplier.get(), inputData.getTruckId(), inputData.getName(),
                    inputData.getDescription(), cents, inputData.getImageReference(), true, false);
        }
        catch (IllegalArgumentException exception) {
            presenter.prepareFailView(exception.getMessage());
            return;
        }
        try {
            dataAccess.save(item);
        }
        catch (DataAccessException exception) {
            presenter.prepareFailView("Could not save the menu item. Please try again.");
            return;
        }
        presenter.prepareSuccessView(new AddMenuItemOutputData(new MenuItemData(item)));
    }
}
