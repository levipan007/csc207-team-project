package interface_adapter.menu_management;

import interface_adapter.menu_management.sales_summary.SalesSummaryViewModel;
import interface_adapter.menu_management.view_sales_summary.ViewSalesSummaryController;
import interface_adapter.menu_management.view_sales_summary.ViewSalesSummaryPresenter;
import interface_adapter.session.CurrentSession;
import java.time.LocalDate;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import use_case.menu_management.view_sales_summary.ViewSalesSummaryInputData;
import use_case.menu_management.view_sales_summary.ViewSalesSummaryOutputData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SalesSummaryAdaptersTest {
    @Test
    void dateParsingRejectsInvalidCalendarDatesAndPassesTypedDatesToUseCase() {
        SalesSummaryViewModel model = new SalesSummaryViewModel();
        AtomicReference<ViewSalesSummaryInputData> received = new AtomicReference<>();
        CurrentSession session = new CurrentSession();
        session.logIn("owner-A", "truck-A");
        ViewSalesSummaryController controller = new ViewSalesSummaryController(received::set, session, model);
        controller.execute("2026-02-30", "2026-03-01");
        assertNull(received.get());
        assertTrue(model.getState().isError());
        controller.execute("2026-11-01", "2026-11-20");
        assertEquals("truck-A", received.get().getTruckId());
        assertEquals(LocalDate.of(2026, 11, 1), received.get().getStartDate());
        assertEquals(LocalDate.of(2026, 11, 20), received.get().getEndDate());
        assertEquals("2026-11-01 to 2026-11-20", model.getState().getRange());
        received.set(null);
        controller.execute(null, "2026-11-20");
        assertNull(received.get());
        session.logIn("owner-B", "truck-B");
        controller.execute("2026-11-01", "2026-11-20");
        assertEquals("truck-B", received.get().getTruckId());
    }

    @Test
    void failedReportDoesNotLeaveOldTotalsDisplayedAsNewResults() {
        SalesSummaryViewModel model = new SalesSummaryViewModel();
        AtomicInteger calls = new AtomicInteger();
        model.addListener(calls::incrementAndGet);
        ViewSalesSummaryPresenter presenter = new ViewSalesSummaryPresenter(model);
        presenter.prepareSuccessView(new ViewSalesSummaryOutputData(950, 1, 1, Collections.emptyList()));
        assertNotNull(model.getState().getReport());
        presenter.prepareFailView("Could not read sales.");
        assertNull(model.getState().getReport());
        assertTrue(model.getState().isError());
        assertEquals(2, calls.get());
        presenter.prepareSuccessView(new ViewSalesSummaryOutputData(0, 0, 0, Collections.emptyList()));
        assertFalse(model.getState().isError());
        assertEquals("No completed sales in this date range.", model.getState().getMessage());
        assertEquals(3, calls.get());
    }
}
