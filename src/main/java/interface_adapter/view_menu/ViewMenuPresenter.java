package interface_adapter.view_menu;

import interface_adapter.menu.MenuPresenterSupport;
import interface_adapter.menu.MenuViewModel;
import use_case.view_menu.ViewMenuOutputBoundary;
import use_case.view_menu.ViewMenuOutputData;

/** Updates the shared menu display after this action completes. */
public final class ViewMenuPresenter extends MenuPresenterSupport implements ViewMenuOutputBoundary {
    public ViewMenuPresenter(MenuViewModel viewModel) { super(viewModel); }
    @Override
    public void prepareSuccessView(ViewMenuOutputData output) {
        viewModel.getState().setItems(output.getItems());
        viewModel.getState().success(output.getItems().isEmpty()
                ? "Add your first menu item to get started." : "Menu refreshed.", false);
        viewModel.notifyStateChanged();
    }
}
