package use_case.edit_menu_item;

import entity.MenuItem;

/** Storage operations needed by this use case. */
public interface EditMenuItemDataAccessInterface {
    /** Returns the matching item, or null if it does not exist. */
    MenuItem get(String itemId);

    /** Saves an immutable replacement; failure must leave the stored item unchanged. */
    void save(MenuItem item);
}
