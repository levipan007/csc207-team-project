package use_case.menu_management.edit_menu_item;

import java.util.Objects;
import use_case.menu_management.menu.MenuItemData;

/** The item successfully saved by this action. */
public final class EditMenuItemOutputData {
    private final MenuItemData item;

    public EditMenuItemOutputData(MenuItemData item) {
        this.item = Objects.requireNonNull(item, "item");
    }

    public MenuItemData getItem() { return item; }
}
