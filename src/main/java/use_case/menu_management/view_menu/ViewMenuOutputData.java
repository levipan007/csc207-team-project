package use_case.menu_management.view_menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import use_case.menu_management.menu.MenuItemData;

/** A snapshot of the truck's menu, including archived items for owner filtering. */
public final class ViewMenuOutputData {
    private final List<MenuItemData> items;

    public ViewMenuOutputData(List<MenuItemData> items) {
        this.items = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(items, "items")));
    }

    public List<MenuItemData> getItems() { return items; }
}
