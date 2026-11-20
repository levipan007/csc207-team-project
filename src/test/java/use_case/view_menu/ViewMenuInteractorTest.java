package use_case.view_menu;

import entity.MenuItem;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import use_case.menu.MenuItemData;
import use_case.menu.MenuTestSupport;
import use_case.menu.MenuTestSupport.FakeDataAccess;
import use_case.menu.MenuTestSupport.RecordingPresenter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewMenuInteractorTest {
    private FakeDataAccess data;
    private RecordingPresenter presenter;
    private ViewMenuInteractor interactor;

    @BeforeEach void setUp() {
        data = new FakeDataAccess();
        presenter = new RecordingPresenter();
        interactor = new ViewMenuInteractor(data, presenter);
    }

    @Test void showsOnlyRequestedTruckIncludingItsArchivedItems() {
        data.items.put("item-1", MenuTestSupport.item());
        data.items.put("archived", new MenuItem("archived", "truck-1", "Old", "", 100, "", false, true));
        data.items.put("foreign", new MenuItem("foreign", "truck-2", "Other", "", 100, "", true, false));
        interactor.execute(new ViewMenuInputData(" truck-1 "));
        presenter.assertSuccess();
        assertEquals("truck-1", data.requestedTruckId);
        assertEquals(2, presenter.items.size());
        assertTrue(presenter.items.stream().allMatch(item -> "truck-1".equals(item.getTruckId())));
        assertTrue(presenter.items.stream().anyMatch(MenuItemData::isArchived));
        assertThrows(UnsupportedOperationException.class, () -> presenter.items.clear());
        data.items.clear();
        assertEquals(2, presenter.items.size(), "Results remain a stable snapshot");
    }

    @Test void emptyMenuIsSuccessfulRatherThanAnError() {
        interactor.execute(new ViewMenuInputData("truck-1"));
        presenter.assertSuccess();
        assertTrue(presenter.items.isEmpty());
    }

    @ParameterizedTest @NullAndEmptySource @ValueSource(strings = {" "})
    void rejectsMissingTruckBeforeLoading(String truck) {
        interactor.execute(new ViewMenuInputData(truck));
        presenter.assertFailure();
        assertEquals(0, data.readCalls);
    }

    @Test void storageFailureIsReported() {
        data.failRead = true;
        interactor.execute(new ViewMenuInputData("truck-1"));
        presenter.assertFailure();
    }

    @Test void nullRequestIsReported() {
        interactor.execute(null);
        presenter.assertFailure();
        assertEquals(0, data.readCalls);
    }

    @Test void outputCopiesItsListSoCallerCannotChangeResults() {
        List<MenuItemData> source = new ArrayList<>();
        source.add(new MenuItemData(MenuTestSupport.item()));
        ViewMenuOutputData output = new ViewMenuOutputData(source);
        source.clear();
        assertEquals(1, output.getItems().size());
        assertThrows(UnsupportedOperationException.class, () -> output.getItems().clear());
    }
}
