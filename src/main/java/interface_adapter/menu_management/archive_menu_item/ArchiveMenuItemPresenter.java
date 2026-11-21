package interface_adapter.menu_management.archive_menu_item;

import interface_adapter.menu_management.menu.MenuPresenterSupport;
import interface_adapter.menu_management.menu.MenuViewModel;
import use_case.menu_management.archive_menu_item.ArchiveMenuItemOutputBoundary;
import use_case.menu_management.archive_menu_item.ArchiveMenuItemOutputData;

/** Updates the shared menu display after this action completes. */
public final class ArchiveMenuItemPresenter extends MenuPresenterSupport implements ArchiveMenuItemOutputBoundary {
    public ArchiveMenuItemPresenter(MenuViewModel viewModel) { super(viewModel); }
    @Override
    public void prepareSuccessView(ArchiveMenuItemOutputData output) {
        presentItem(output.getItem(), "Menu item archived. Existing orders keep their original details.", false);
    }
}
