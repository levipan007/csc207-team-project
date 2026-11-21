package use_case.menu_management.archive_menu_item;

import java.util.Objects;
import use_case.menu_management.menu.MenuItemData;

/** The item successfully saved by this action. */
public final class ArchiveMenuItemOutputData {
    private final MenuItemData item;

    public ArchiveMenuItemOutputData(MenuItemData item) {
        this.item = Objects.requireNonNull(item, "item");
    }

    public MenuItemData getItem() { return item; }
}
