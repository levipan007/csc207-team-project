package use_case.menu_management.change_availability;

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

class ChangeAvailabilityInteractorTest {
    private FakeDataAccess data;
    private RecordingPresenter presenter;
    private ChangeAvailabilityInteractor interactor;
    private MenuItem original;

    @BeforeEach void setUp() {
        data = new FakeDataAccess();
        presenter = new RecordingPresenter();
        interactor = new ChangeAvailabilityInteractor(data, presenter);
        original = MenuTestSupport.item();
        data.items.put("item-1", original);
    }

    private ChangeAvailabilityInputData input(String truck, String id) {
        return new ChangeAvailabilityInputData(truck, id, false);
    }

    @Test void savesRequestedStateAndKeepsOriginalUnchanged() {
        interactor.execute(input("truck-1", "item-1"));
        presenter.assertSuccess();
        assertEquals(1, data.saveCalls);
        assertEquals("item-1", presenter.item.getId());
        assertEquals("Burger", presenter.item.getName());
        assertFalse(presenter.item.isAvailable());
        assertFalse(presenter.item.isArchived());
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

    @Test void archivedItemCannotBeReenabled() {
        data.items.put("item-1", original.archive());
        interactor.execute(new ChangeAvailabilityInputData("truck-1", "item-1", true));
        presenter.assertFailure();
        assertEquals(0, data.saveCalls);
        assertTrue(data.items.get("item-1").isArchived());
        assertFalse(data.items.get("item-1").isAvailable());
    }

    @Test void soldOutItemCanBecomeAvailableAgain() {
        data.items.put("item-1", original.withAvailability(false));
        interactor.execute(new ChangeAvailabilityInputData("truck-1", "item-1", true));
        presenter.assertSuccess();
        assertTrue(presenter.item.isAvailable());
    }
}
