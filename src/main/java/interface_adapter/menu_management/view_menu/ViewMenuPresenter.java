package interface_adapter.menu_management.view_menu;

import interface_adapter.menu_management.menu.MenuPresenterSupport;
import interface_adapter.menu_management.menu.MenuViewModel;
import use_case.menu_management.view_menu.ViewMenuOutputBoundary;
import use_case.menu_management.view_menu.ViewMenuOutputData;

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
