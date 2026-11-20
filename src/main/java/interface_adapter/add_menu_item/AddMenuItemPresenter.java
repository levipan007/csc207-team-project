package interface_adapter.add_menu_item;

import interface_adapter.menu.MenuPresenterSupport;
import interface_adapter.menu.MenuViewModel;
import use_case.add_menu_item.AddMenuItemOutputBoundary;
import use_case.add_menu_item.AddMenuItemOutputData;

/** Updates the shared menu display after this action completes. */
public final class AddMenuItemPresenter extends MenuPresenterSupport implements AddMenuItemOutputBoundary {
    public AddMenuItemPresenter(MenuViewModel viewModel) { super(viewModel); }
    @Override
    public void prepareSuccessView(AddMenuItemOutputData output) {
        presentItem(output.getItem(), "Menu item added.", true);
    }
}
