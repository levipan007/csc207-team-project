package use_case.add_menu_item;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import use_case.menu.MenuTestSupport.FakeDataAccess;
import use_case.menu.MenuTestSupport.RecordingPresenter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddMenuItemInteractorTest {
    private FakeDataAccess data;
    private RecordingPresenter presenter;
    private AddMenuItemInteractor interactor;

    @BeforeEach void setUp() {
        data = new FakeDataAccess();
        presenter = new RecordingPresenter();
        interactor = new AddMenuItemInteractor(data, presenter, () -> "new-id");
    }

    private AddMenuItemInputData input(String price) {
        return new AddMenuItemInputData("truck-1", " Burger ", " Fresh ", price, " burger.png ");
    }

    @Test void savesValidItemAndReportsItsValues() {
        interactor.execute(input("8.99"));
        presenter.assertSuccess();
        assertEquals(1, data.saveCalls);
        assertEquals(1, data.items.size());
        assertEquals(899, data.items.get("new-id").getPriceCents());
        assertEquals("new-id", presenter.item.getId());
        assertEquals("truck-1", presenter.item.getTruckId());
        assertEquals("Burger", presenter.item.getName());
        assertEquals("Fresh", presenter.item.getDescription());
        assertEquals(899, presenter.item.getPriceCents());
        assertEquals("burger.png", presenter.item.getImageReference());
        assertTrue(presenter.item.isAvailable());
        assertFalse(presenter.item.isArchived());
    }

    @ParameterizedTest @NullAndEmptySource
    @ValueSource(strings = {"  ", "abc", "0", "-0.01", "1.001", "1.000", "92233720368547758.08"})
    void rejectsInvalidPriceWithoutSaving(String price) {
        interactor.execute(input(price));
        presenter.assertFailure();
        assertEquals(0, data.saveCalls);
        assertTrue(data.items.isEmpty());
    }

    @Test void convertsDecimalExactlyAndAcceptsMaximumRepresentableCents() {
        interactor.execute(input("0.10"));
        presenter.assertSuccess();
        assertEquals(10, presenter.item.getPriceCents());
        RecordingPresenter second = new RecordingPresenter();
        new AddMenuItemInteractor(data, second, () -> "max-id")
                .execute(input("92233720368547758.07"));
        second.assertSuccess();
        assertEquals(Long.MAX_VALUE, second.item.getPriceCents());
    }

    @Test void rejectsMissingNameAndTruck() {
        interactor.execute(new AddMenuItemInputData("truck-1", " ", "", "1", ""));
        presenter.assertFailure();
        assertEquals(0, data.saveCalls);
        RecordingPresenter second = new RecordingPresenter();
        new AddMenuItemInteractor(data, second).execute(
                new AddMenuItemInputData(null, "Soup", "", "1", ""));
        second.assertFailure();
        assertEquals(0, data.saveCalls);
    }

    @Test void absentRequestProducesFailureCallback() {
        interactor.execute(null);
        presenter.assertFailure();
        assertEquals(0, data.saveCalls);
    }

    @Test void failedSaveNeverReportsSuccess() {
        data.failSave = true;
        interactor.execute(input("8.99"));
        presenter.assertFailure();
        assertEquals(1, data.saveCalls);
        assertTrue(data.items.isEmpty());
    }

    @Test void defaultIdGeneratorCreatesDistinctIds() {
        new AddMenuItemInteractor(data, presenter).execute(input("1"));
        presenter.assertSuccess();
        RecordingPresenter second = new RecordingPresenter();
        new AddMenuItemInteractor(data, second).execute(input("1"));
        second.assertSuccess();
        assertNotEquals(presenter.item.getId(), second.item.getId());
        assertEquals(2, data.items.size());
    }

    @Test void duplicateNamesAreAllowedWhenIdsDiffer() {
        AtomicInteger ids = new AtomicInteger();
        AddMenuItemInteractor sameNameInteractor = new AddMenuItemInteractor(data, presenter,
                () -> "item-" + ids.incrementAndGet());
        sameNameInteractor.execute(input("1"));
        sameNameInteractor.execute(input("2"));
        assertEquals(2, presenter.successes);
        assertEquals(0, presenter.failures);
        assertEquals(2, data.items.size());
    }
}
