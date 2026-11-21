package interface_adapter.menu_management.change_availability;

import interface_adapter.menu_management.menu.MenuPresenterSupport;
import interface_adapter.menu_management.menu.MenuViewModel;
import use_case.menu_management.change_availability.ChangeAvailabilityOutputBoundary;
import use_case.menu_management.change_availability.ChangeAvailabilityOutputData;

/** Updates the shared menu display after this action completes. */
public final class ChangeAvailabilityPresenter extends MenuPresenterSupport implements ChangeAvailabilityOutputBoundary {
    public ChangeAvailabilityPresenter(MenuViewModel viewModel) { super(viewModel); }
    @Override
    public void prepareSuccessView(ChangeAvailabilityOutputData output) {
        presentItem(output.getItem(), "Availability updated.", false);
    }
}
