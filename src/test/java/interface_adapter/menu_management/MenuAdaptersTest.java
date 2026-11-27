package interface_adapter.menu_management;

import entity.MenuItem;
import interface_adapter.menu_management.add_menu_item.AddMenuItemController;
import interface_adapter.menu_management.add_menu_item.AddMenuItemPresenter;
import interface_adapter.menu_management.archive_menu_item.ArchiveMenuItemPresenter;
import interface_adapter.menu_management.change_availability.ChangeAvailabilityPresenter;
import interface_adapter.menu_management.edit_menu_item.EditMenuItemPresenter;
import interface_adapter.menu_management.menu.MenuState;
import interface_adapter.menu_management.menu.MenuViewModel;
import interface_adapter.menu_management.menu.MenuViewModelListener;
import interface_adapter.menu_management.menu.MoneyFormatter;
import interface_adapter.menu_management.view_menu.ViewMenuPresenter;
import interface_adapter.session.CurrentSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import use_case.menu_management.add_menu_item.AddMenuItemInputData;
import use_case.menu_management.add_menu_item.AddMenuItemOutputData;
import use_case.menu_management.archive_menu_item.ArchiveMenuItemOutputData;
import use_case.menu_management.change_availability.ChangeAvailabilityOutputData;
import use_case.menu_management.edit_menu_item.EditMenuItemOutputData;
import use_case.menu_management.menu.MenuItemData;
import use_case.menu_management.view_menu.ViewMenuOutputData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuAdaptersTest {
    @Test
    void presentersNotifyAndUpdateTheSameRowWithoutChangingHistoryOnFailure() {
        MenuViewModel model = new MenuViewModel();
        AtomicInteger notifications = new AtomicInteger();
        model.addListener(notifications::incrementAndGet);
        MenuItem original = item();
        AddMenuItemPresenter add = new AddMenuItemPresenter(model);
        add.prepareSuccessView(new AddMenuItemOutputData(new MenuItemData(original)));
        assertEquals(1, notifications.get());
        assertEquals(1, model.getState().getItems().size());
        assertTrue(model.getState().shouldCloseEditor());

        MenuItem updated = original.withDetails("Big burger", "Lettuce", 1250, "image.png");
        new EditMenuItemPresenter(model).prepareSuccessView(new EditMenuItemOutputData(new MenuItemData(updated)));
        assertEquals(1, model.getState().getItems().size());
        assertEquals("Big burger", model.getState().getItems().get(0).getName());
        long changes = model.getState().getSuccessfulChange();
        add.prepareFailView("Storage is unavailable.");
        assertTrue(model.getState().isError());
        assertFalse(model.getState().shouldCloseEditor());
        assertEquals(changes, model.getState().getSuccessfulChange());
        assertEquals(1250, model.getState().getItems().get(0).getPriceCents());

        new ChangeAvailabilityPresenter(model).prepareSuccessView(
                new ChangeAvailabilityOutputData(new MenuItemData(updated.withAvailability(false))));
        assertFalse(model.getState().getItems().get(0).isAvailable());
        new ArchiveMenuItemPresenter(model).prepareSuccessView(
                new ArchiveMenuItemOutputData(new MenuItemData(updated.archive())));
        assertTrue(model.getState().getItems().get(0).isArchived());
        assertFalse(model.getState().isError());
        assertEquals(5, notifications.get());
    }

    @Test
    void listingReplacesSnapshotAndDoesNotRequireASeparateScreenModel() {
        MenuViewModel model = new MenuViewModel();
        ViewMenuPresenter presenter = new ViewMenuPresenter(model);
        presenter.prepareSuccessView(new ViewMenuOutputData(Collections.singletonList(new MenuItemData(item()))));
        assertEquals(1, model.getState().getItems().size());
        presenter.prepareSuccessView(new ViewMenuOutputData(Collections.emptyList()));
        assertTrue(model.getState().getItems().isEmpty());
        assertTrue(model.getState().getMessage().contains("first menu item"));
        presenter.prepareFailView("Cannot read menu.");
        assertEquals("Cannot read menu.", model.getState().getMessage());
    }

    @Test
    void controllerPassesUserInputAndTruckContextWithoutPretendingToSave() {
        AtomicReference<AddMenuItemInputData> received = new AtomicReference<>();
        CurrentSession session = new CurrentSession();
        session.logIn("owner-A", "truck-A");
        new AddMenuItemController(received::set, session)
                .execute("Burger", "Lettuce", "9.50", "photo.png");
        assertEquals("truck-A", received.get().getTruckId());
        assertEquals("Burger", received.get().getName());
        assertEquals("Lettuce", received.get().getDescription());
        assertEquals("9.50", received.get().getPrice());
        assertEquals("photo.png", received.get().getImageReference());
    }

    @Test
    void controllerUsesTheTruckThatIsLoggedInWhenItRuns() {
        AtomicReference<AddMenuItemInputData> received = new AtomicReference<>();
        CurrentSession session = new CurrentSession();
        session.logIn("owner-A", "truck-A");
        AddMenuItemController controller = new AddMenuItemController(received::set, session);
        session.logIn("owner-B", "truck-B");
        controller.execute("Burger", "", "9.50", "");
        assertEquals("truck-B", received.get().getTruckId());
    }

    @Test
    void viewModelUsesExplicitNotificationsAndImmutableRowLists() {
        MenuViewModel model = new MenuViewModel();
        AtomicInteger calls = new AtomicInteger();
        MenuViewModelListener listener = calls::incrementAndGet;
        model.addListener(listener);
        MenuState state = new MenuState();
        model.setState(state);
        assertEquals(0, calls.get());
        assertSame(state, model.getState());
        List<MenuItemData> rows = new ArrayList<>();
        rows.add(new MenuItemData(item()));
        state.setItems(rows);
        rows.clear();
        assertEquals(1, state.getItems().size());
        assertThrows(UnsupportedOperationException.class, () -> state.getItems().clear());
        model.notifyStateChanged();
        assertEquals(1, calls.get());
        model.removeListener(listener);
        model.notifyStateChanged();
        assertEquals(1, calls.get());
        assertEquals("9.50", MoneyFormatter.input(950));
        assertTrue(MoneyFormatter.format(950).contains("9.50"));
    }

    private MenuItem item() {
        return new MenuItem("item-1", "truck-A", "Burger", "", 950, "", true, false);
    }
}
