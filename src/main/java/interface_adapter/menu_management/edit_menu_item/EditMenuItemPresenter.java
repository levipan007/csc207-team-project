package interface_adapter.menu_management.edit_menu_item;

import interface_adapter.menu_management.menu.MenuPresenterSupport;
import interface_adapter.menu_management.menu.MenuViewModel;
import use_case.menu_management.edit_menu_item.EditMenuItemOutputBoundary;
import use_case.menu_management.edit_menu_item.EditMenuItemOutputData;

/** Updates the shared menu display after this action completes. */
public final class EditMenuItemPresenter extends MenuPresenterSupport implements EditMenuItemOutputBoundary {
    public EditMenuItemPresenter(MenuViewModel viewModel) { super(viewModel); }
    @Override
    public void prepareSuccessView(EditMenuItemOutputData output) {
        presentItem(output.getItem(), "Menu item updated.", true);
    }
}
