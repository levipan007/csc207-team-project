package interface_adapter.change_availability;

import interface_adapter.menu.MenuPresenterSupport;
import interface_adapter.menu.MenuViewModel;
import use_case.change_availability.ChangeAvailabilityOutputBoundary;
import use_case.change_availability.ChangeAvailabilityOutputData;

/** Updates the shared menu display after this action completes. */
public final class ChangeAvailabilityPresenter extends MenuPresenterSupport implements ChangeAvailabilityOutputBoundary {
    public ChangeAvailabilityPresenter(MenuViewModel viewModel) { super(viewModel); }
    @Override
    public void prepareSuccessView(ChangeAvailabilityOutputData output) {
        presentItem(output.getItem(), "Availability updated.", false);
    }
}
