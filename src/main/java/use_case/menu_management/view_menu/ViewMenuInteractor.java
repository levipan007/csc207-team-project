package use_case.menu_management.view_menu;

import entity.MenuItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import use_case.shared.DataAccessException;
import use_case.menu_management.menu.MenuItemData;
import use_case.menu_management.menu.MenuValidation;

/** Loads menu output values for one truck, retaining archived records for owner filters. */
public final class ViewMenuInteractor implements ViewMenuInputBoundary {
    private final ViewMenuDataAccessInterface dataAccess;
    private final ViewMenuOutputBoundary presenter;

    public ViewMenuInteractor(ViewMenuDataAccessInterface dataAccess,
                              ViewMenuOutputBoundary presenter) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess");
        this.presenter = Objects.requireNonNull(presenter, "presenter");
    }

    @Override
    public void execute(ViewMenuInputData inputData) {
        if (inputData == null) {
            presenter.prepareFailView("A truck is required to view its menu.");
            return;
        }
        final String truckId;
        try {
            truckId = MenuValidation.requireText(inputData.getTruckId(), "Truck ID");
        }
        catch (IllegalArgumentException exception) {
            presenter.prepareFailView(exception.getMessage());
            return;
        }
        final List<MenuItem> items;
        try {
            items = dataAccess.getAllForTruck(truckId);
        }
        catch (DataAccessException exception) {
            presenter.prepareFailView("Could not load the menu. Please try again.");
            return;
        }
        List<MenuItemData> output = new ArrayList<>();
        for (MenuItem item : items) {
            if (truckId.equals(item.getTruckId())) {
                output.add(new MenuItemData(item));
            }
        }
        presenter.prepareSuccessView(new ViewMenuOutputData(output));
    }
}
