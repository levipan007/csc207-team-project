package use_case.edit_menu_item;

import entity.MenuItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import use_case.menu.MenuTestSupport;
import use_case.menu.MenuTestSupport.FakeDataAccess;
import use_case.menu.MenuTestSupport.RecordingPresenter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditMenuItemInteractorTest {
    private FakeDataAccess data;
    private RecordingPresenter presenter;
    private EditMenuItemInteractor interactor;
    private MenuItem original;

    @BeforeEach void setUp() {
        data = new FakeDataAccess();
        presenter = new RecordingPresenter();
        interactor = new EditMenuItemInteractor(data, presenter);
        original = MenuTestSupport.item();
        data.items.put(original.getId(), original);
    }

    private EditMenuItemInputData input(String truck, String id, String price) {
        return new EditMenuItemInputData(truck, id, "Soup", "Hot soup", price, "soup.png");
    }

    @Test void updatesSameIdWithoutMutatingOriginal() {
        interactor.execute(input("truck-1", "item-1", "10.50"));
        presenter.assertSuccess();
        assertEquals(1, data.items.size());
        assertEquals("item-1", presenter.item.getId());
        assertEquals("Soup", data.items.get("item-1").getName());
        assertEquals(1050, presenter.item.getPriceCents());
        assertEquals("Burger", original.getName());
        assertEquals(899, original.getPriceCents());
    }

    @Test void saveFailureLeavesStoredItemUnchanged() {
        data.failSave = true;
        interactor.execute(input("truck-1", "item-1", "10.50"));
        presenter.assertFailure();
        assertSame(original, data.items.get("item-1"));
        assertEquals(899, original.getPriceCents());
    }

    @ParameterizedTest @ValueSource(strings = {"truck-2", ""})
    void cannotEditOtherTrucksItem(String truck) {
        interactor.execute(input(truck, "item-1", "10"));
        presenter.assertFailure();
        assertEquals(0, data.saveCalls);
        assertSame(original, data.items.get("item-1"));
    }

    @ParameterizedTest @ValueSource(strings = {"missing", ""})
    void cannotEditMissingItem(String id) {
        interactor.execute(input("truck-1", id, "10"));
        presenter.assertFailure();
        assertEquals(0, data.saveCalls);
    }

    @Test void cannotEditArchivedItem() {
        data.items.put("item-1", original.archive());
        interactor.execute(input("truck-1", "item-1", "10"));
        presenter.assertFailure();
        assertEquals(0, data.saveCalls);
        assertTrue(data.items.get("item-1").isArchived());
    }

    @ParameterizedTest @ValueSource(strings = {"0", "10.999", "not a price"})
    void invalidDetailsNeverReachSave(String price) {
        interactor.execute(input("truck-1", "item-1", price));
        presenter.assertFailure();
        assertEquals(0, data.saveCalls);
        assertSame(original, data.items.get("item-1"));
    }

    @Test void missingNameIsRejected() {
        interactor.execute(new EditMenuItemInputData("truck-1", "item-1", "", "", "10", ""));
        presenter.assertFailure();
        assertEquals(0, data.saveCalls);
    }

    @Test void unavailableItemRemainsUnavailableAfterEditing() {
        data.items.put("item-1", original.withAvailability(false));
        interactor.execute(input("truck-1", "item-1", "10"));
        presenter.assertSuccess();
        assertFalse(presenter.item.isAvailable());
    }

    @Test void failedReadReportsFailureWithoutSave() {
        data.failRead = true;
        interactor.execute(input("truck-1", "item-1", "10"));
        presenter.assertFailure();
        assertEquals(0, data.saveCalls);
    }

    @Test void nullRequestReportsFailure() {
        interactor.execute(null);
        presenter.assertFailure();
        assertEquals(0, data.readCalls);
    }
}
