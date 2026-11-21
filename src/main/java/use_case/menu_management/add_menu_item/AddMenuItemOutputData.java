package use_case.menu_management.add_menu_item;

import java.util.Objects;
import use_case.menu_management.menu.MenuItemData;

/** The item successfully saved by this action. */
public final class AddMenuItemOutputData {
    private final MenuItemData item;

    public AddMenuItemOutputData(MenuItemData item) {
        this.item = Objects.requireNonNull(item, "item");
    }

    public MenuItemData getItem() { return item; }
}
