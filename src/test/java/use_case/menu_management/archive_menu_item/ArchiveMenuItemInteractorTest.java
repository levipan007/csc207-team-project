package use_case.menu_management.archive_menu_item;

import entity.MenuItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import use_case.menu_management.menu.MenuTestSupport;
import use_case.menu_management.menu.MenuTestSupport.FakeDataAccess;
import use_case.menu_management.menu.MenuTestSupport.RecordingPresenter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchiveMenuItemInteractorTest {
    private FakeDataAccess data;
    private RecordingPresenter presenter;
    private ArchiveMenuItemInteractor interactor;
    private MenuItem original;

    @BeforeEach void setUp() {
        data = new FakeDataAccess();
        presenter = new RecordingPresenter();
        interactor = new ArchiveMenuItemInteractor(data, presenter);
        original = MenuTestSupport.item();
        data.items.put("item-1", original);
    }

    private ArchiveMenuItemInputData input(String truck, String id) {
        return new ArchiveMenuItemInputData(truck, id);
    }

    @Test void savesRequestedStateAndKeepsOriginalUnchanged() {
        interactor.execute(input("truck-1", "item-1"));
        presenter.assertSuccess();
        assertEquals(1, data.saveCalls);
        assertEquals("item-1", presenter.item.getId());
        assertEquals("Burger", presenter.item.getName());
        assertTrue(presenter.item.isArchived());
        assertFalse(presenter.item.isAvailable());
        assertTrue(original.isAvailable());
        assertFalse(original.isArchived());
    }

    @Test void saveFailureDoesNotMutateStoredState() {
        data.failSave = true;
        interactor.execute(input("truck-1", "item-1"));
        presenter.assertFailure();
        assertSame(original, data.items.get("item-1"));
        assertTrue(original.isAvailable());
        assertFalse(original.isArchived());
    }

    @ParameterizedTest @ValueSource(strings = {"truck-2", ""})
    void cannotChangeOtherTrucksItem(String truck) {
        interactor.execute(input(truck, "item-1"));
        presenter.assertFailure();
        assertEquals(0, data.saveCalls);
    }

    @ParameterizedTest @ValueSource(strings = {"missing", ""})
    void cannotChangeMissingItem(String id) {
        interactor.execute(input("truck-1", id));
        presenter.assertFailure();
        assertEquals(0, data.saveCalls);
    }

    @Test void failedReadNeverAttemptsSave() {
        data.failRead = true;
        interactor.execute(input("truck-1", "item-1"));
        presenter.assertFailure();
        assertEquals(0, data.saveCalls);
    }

    @Test void nullRequestReportsFailure() {
        interactor.execute(null);
        presenter.assertFailure();
        assertEquals(0, data.readCalls);
    }

    @Test void alreadyArchivedItemSucceedsWithoutChangingItsHistory() {
        MenuItem archived = original.archive();
        data.items.put("item-1", archived);
        interactor.execute(input("truck-1", "item-1"));
        presenter.assertSuccess();
        assertSame(archived, data.items.get("item-1"));
        assertEquals(899, presenter.item.getPriceCents());
    }
}
