package use_case.view_menu;

import entity.MenuItem;
import java.util.List;

/** Loads a truck's menu, including archived records. */
public interface ViewMenuDataAccessInterface {
    List<MenuItem> getAllForTruck(String truckId);
}
