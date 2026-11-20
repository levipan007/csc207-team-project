package use_case.change_availability;

import java.util.Objects;
import use_case.menu.MenuItemData;

/** The item successfully saved by this action. */
public final class ChangeAvailabilityOutputData {
    private final MenuItemData item;

    public ChangeAvailabilityOutputData(MenuItemData item) {
        this.item = Objects.requireNonNull(item, "item");
    }

    public MenuItemData getItem() { return item; }
}
