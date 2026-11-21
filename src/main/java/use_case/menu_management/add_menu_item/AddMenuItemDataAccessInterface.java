package use_case.menu_management.add_menu_item;

import entity.MenuItem;

/** Storage operations needed by this use case. */
public interface AddMenuItemDataAccessInterface {
    /** Saves an immutable replacement; failure must leave the stored item unchanged. */
    void save(MenuItem item);
}
