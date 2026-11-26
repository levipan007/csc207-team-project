package use_case.menu_management.menu;

import entity.MenuItem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import use_case.menu_management.add_menu_item.AddMenuItemDataAccessInterface;
import use_case.menu_management.add_menu_item.AddMenuItemOutputBoundary;
import use_case.menu_management.add_menu_item.AddMenuItemOutputData;
import use_case.menu_management.edit_menu_item.EditMenuItemDataAccessInterface;
import use_case.menu_management.edit_menu_item.EditMenuItemOutputBoundary;
import use_case.menu_management.edit_menu_item.EditMenuItemOutputData;
import use_case.menu_management.archive_menu_item.ArchiveMenuItemDataAccessInterface;
import use_case.menu_management.archive_menu_item.ArchiveMenuItemOutputBoundary;
import use_case.menu_management.archive_menu_item.ArchiveMenuItemOutputData;
import use_case.menu_management.change_availability.ChangeAvailabilityDataAccessInterface;
import use_case.menu_management.change_availability.ChangeAvailabilityOutputBoundary;
import use_case.menu_management.change_availability.ChangeAvailabilityOutputData;
import use_case.menu_management.view_menu.ViewMenuDataAccessInterface;
import use_case.shared.DataAccessException;
import use_case.menu_management.view_menu.ViewMenuOutputBoundary;
import use_case.menu_management.view_menu.ViewMenuOutputData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Recording collaborators let tests check that the expected callback actually occurred. */
public final class MenuTestSupport {
    private MenuTestSupport() { }

    public static MenuItem item() {
        return new MenuItem("item-1", "truck-1", "Burger", "Original", 899,
                "images/burger.png", true, false);
    }

    public static final class FakeDataAccess implements AddMenuItemDataAccessInterface,
            EditMenuItemDataAccessInterface, ArchiveMenuItemDataAccessInterface,
            ChangeAvailabilityDataAccessInterface, ViewMenuDataAccessInterface {
        public final Map<String, MenuItem> items = new LinkedHashMap<>();
        public boolean failSave;
        public boolean failRead;
        public int saveCalls;
        public int readCalls;
        public String requestedTruckId;

        @Override
        public void save(MenuItem item) {
            saveCalls++;
            if (failSave) {
                throw new DataAccessException("Simulated save failure");
            }
            items.put(item.getId(), item);
        }

        @Override
        public MenuItem get(String itemId) {
            readCalls++;
            if (failRead) {
                throw new DataAccessException("Simulated read failure");
            }
            return items.get(itemId);
        }

        @Override
        public List<MenuItem> getAllForTruck(String truckId) {
            readCalls++;
            requestedTruckId = truckId;
            if (failRead) {
                throw new DataAccessException("Simulated read failure");
            }
            // Deliberately returns all rows: the interactor must not expose another truck's data.
            return new ArrayList<>(items.values());
        }
    }

    public static final class RecordingPresenter implements AddMenuItemOutputBoundary,
            EditMenuItemOutputBoundary, ArchiveMenuItemOutputBoundary,
            ChangeAvailabilityOutputBoundary, ViewMenuOutputBoundary {
        public int successes;
        public int failures;
        public String error;
        public MenuItemData item;
        public List<MenuItemData> items;

        @Override public void prepareSuccessView(AddMenuItemOutputData output) {
            successes++;
        item = output.getItem();
        }
        @Override public void prepareSuccessView(EditMenuItemOutputData output) {
            successes++;
        item = output.getItem();
        }
        @Override public void prepareSuccessView(ArchiveMenuItemOutputData output) {
            successes++;
        item = output.getItem();
        }
        @Override public void prepareSuccessView(ChangeAvailabilityOutputData output) {
            successes++;
        item = output.getItem();
        }
        @Override public void prepareSuccessView(ViewMenuOutputData output) {
            successes++;
        items = output.getItems();
        }
        @Override public void prepareFailView(String message) {
            failures++;
        error = message;
        }

        public void assertSuccess() {
            assertEquals(1, successes, "A success callback must actually be called exactly once");
            assertEquals(0, failures, "A success must not also report failure");
        }

        public void assertFailure() {
            assertEquals(0, successes, "A failure must not also report success");
            assertEquals(1, failures, "A failure callback must actually be called exactly once");
            assertNotNull(error);
            assertFalse(error.trim().isEmpty());
        }
    }
}
