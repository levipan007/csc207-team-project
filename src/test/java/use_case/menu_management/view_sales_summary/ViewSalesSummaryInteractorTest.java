package use_case.menu_management.view_sales_summary;

import entity.CompletedSale;
import entity.SaleLine;
import org.junit.jupiter.api.Test;
import use_case.menu_management.menu.DataAccessException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ViewSalesSummaryInteractorTest {
    private static final LocalDate DAY = LocalDate.of(2026, 11, 20);

    @Test
    void emptySalesProduceAnExplicitSuccessfulZeroReport() {
        final RecordingPresenter presenter = new RecordingPresenter();
        new ViewSalesSummaryInteractor(truck -> Collections.emptyList(), presenter).execute(input(DAY, DAY));
        assertEquals(1, presenter.successCalls);
        assertNull(presenter.error);
        assertEquals(0, presenter.result.getTotalRevenueCents());
        assertEquals(0, presenter.result.getOrderCount());
        assertEquals(0, presenter.result.getItemCount());
        assertTrue(presenter.result.getTopItems().isEmpty());
    }

    @Test
    void reportUsesInclusiveTorontoDatesAndPurchasePricesAcrossRenames() {
        final List<CompletedSale> sales = Arrays.asList(
                sale("before", "truck-a", "2026-11-20T04:59:59Z", line("burger", "Old burger", 999, 9)),
                sale("start", "truck-a", "2026-11-20T05:00:00Z", line("burger", "Old burger", 1000, 2)),
                sale("end", "truck-a", "2026-11-21T04:59:59.999999999Z", line("burger", "New burger", 1250, 1),
                        line("fries", "Fries", 400, 4)),
                sale("after", "truck-a", "2026-11-21T05:00:00Z", line("burger", "New burger", 1250, 9)),
                sale("other", "truck-b", "2026-11-20T12:00:00Z", line("burger", "Other truck", 100, 30)));
        final RecordingPresenter presenter = new RecordingPresenter();
        new ViewSalesSummaryInteractor(truck -> sales, presenter).execute(input(DAY, DAY));
        assertEquals(1, presenter.successCalls);
        assertEquals(4850, presenter.result.getTotalRevenueCents());
        assertEquals(2, presenter.result.getOrderCount());
        assertEquals(7, presenter.result.getItemCount());
        final TopItemData first = presenter.result.getTopItems().get(0);
        assertEquals("fries", first.getItemId());
        assertEquals(4, first.getQuantity());
        final TopItemData second = presenter.result.getTopItems().get(1);
        assertEquals("New burger", second.getName());
        assertEquals(3250, second.getRevenueCents());
        assertThrows(UnsupportedOperationException.class, () -> presenter.result.getTopItems().clear());
    }

    @Test
    void timezoneIsConfigurableAndDaylightSavingOverlapCountsBothOrders() {
        final List<CompletedSale> sales = Arrays.asList(
                sale("one", "truck-a", "2026-11-01T05:30:00Z", line("one", "One", 100, 1)),
                sale("two", "truck-a", "2026-11-01T06:30:00Z", line("two", "Two", 200, 1)));
        final RecordingPresenter presenter = new RecordingPresenter();
        new ViewSalesSummaryInteractor(truck -> sales, presenter)
                .execute(input(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1)));
        assertEquals(2, presenter.result.getOrderCount());
        assertEquals("two", presenter.result.getTopItems().get(0).getItemId());

        final RecordingPresenter utcPresenter = new RecordingPresenter();
        new ViewSalesSummaryInteractor(truck -> Collections.singletonList(
                sale("utc", "truck-a", "2026-11-20T01:00:00Z", line("one", "One", 100, 1))),
                utcPresenter, ZoneId.of("UTC")).execute(input(DAY, DAY));
        assertEquals(1, utcPresenter.result.getOrderCount());
    }

    @Test
    void equalQuantityAndRevenueHaveStableItemIdOrder() {
        final RecordingPresenter presenter = new RecordingPresenter();
        new ViewSalesSummaryInteractor(truck -> Collections.singletonList(
                sale("sale", "truck-a", "2026-11-20T12:00:00Z",
                        line("z", "Z", 500, 1), line("a", "A", 500, 1))), presenter)
                .execute(input(DAY, DAY));
        assertEquals("a", presenter.result.getTopItems().get(0).getItemId());
    }

    @Test
    void invalidRequestsFailBeforeReadingStorage() {
        final List<ViewSalesSummaryInputData> invalid = Arrays.asList(null,
                new ViewSalesSummaryInputData(null, DAY, DAY),
                new ViewSalesSummaryInputData(" ", DAY, DAY), input(null, DAY), input(DAY, null),
                input(DAY.plusDays(1), DAY));
        for (ViewSalesSummaryInputData request : invalid) {
            final RecordingPresenter presenter = new RecordingPresenter();
            new ViewSalesSummaryInteractor(truck -> {
                fail("Invalid requests must not access storage.");
                return Collections.emptyList();
            }, presenter).execute(request);
            assertEquals(0, presenter.successCalls);
            assertEquals(1, presenter.failureCalls);
            assertNotNull(presenter.error);
        }
    }

    @Test
    void storageFailureDoesNotBecomeAnEmptySuccessfulReport() {
        final RecordingPresenter presenter = new RecordingPresenter();
        new ViewSalesSummaryInteractor(truck -> {
            throw new DataAccessException("Malformed completed-sales.json.");
        }, presenter).execute(input(DAY, DAY));
        assertEquals(0, presenter.successCalls);
        assertEquals(1, presenter.failureCalls);
        assertTrue(presenter.error.contains("Malformed"));
    }

    @Test
    void overflowingRevenueFailsInsteadOfWrappingNegative() {
        final RecordingPresenter presenter = new RecordingPresenter();
        new ViewSalesSummaryInteractor(truck -> Collections.singletonList(
                sale("sale", "truck-a", "2026-11-20T12:00:00Z",
                        line("a", "A", Long.MAX_VALUE, 1), line("b", "B", 1, 1))), presenter)
                .execute(input(DAY, DAY));
        assertEquals(1, presenter.failureCalls);
        assertEquals(0, presenter.successCalls);
        assertTrue(presenter.error.contains("too large"));
    }

    private static ViewSalesSummaryInputData input(LocalDate start, LocalDate end) {
        return new ViewSalesSummaryInputData("truck-a", start, end);
    }

    private static SaleLine line(String id, String name, long price, int quantity) {
        return new SaleLine(id, name, price, quantity);
    }

    private static CompletedSale sale(String id, String truck, String time, SaleLine... lines) {
        return new CompletedSale(id, truck, Instant.parse(time), Arrays.asList(lines));
    }

    private static final class RecordingPresenter implements ViewSalesSummaryOutputBoundary {
        private ViewSalesSummaryOutputData result;
        private String error;
        private int successCalls;
        private int failureCalls;

        @Override
        public void prepareSuccessView(ViewSalesSummaryOutputData outputData) {
            successCalls++;
            result = outputData;
        }

        @Override
        public void prepareFailView(String message) {
            failureCalls++;
            error = message;
        }
    }
}
