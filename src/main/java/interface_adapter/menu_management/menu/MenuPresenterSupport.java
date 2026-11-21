package interface_adapter.menu_management.menu;

import use_case.menu_management.menu.MenuItemData;

/** Common display updates for the menu action presenters. */
public abstract class MenuPresenterSupport {
    protected final MenuViewModel viewModel;

    protected MenuPresenterSupport(MenuViewModel viewModel) { this.viewModel = viewModel; }

    protected void presentItem(MenuItemData item, String message, boolean closeEditor) {
        viewModel.getState().upsert(item);
        viewModel.getState().success(message, closeEditor);
        viewModel.notifyStateChanged();
    }

    public void prepareFailView(String error) {
        viewModel.getState().fail(error);
        viewModel.notifyStateChanged();
    }
}
