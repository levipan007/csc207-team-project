package interface_adapter.ordering.order_board;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderBoardViewModelTest {
    private static final Instant PLACED = Instant.parse("2026-11-25T17:00:00Z");

    @Test
    void listenersAreNotifiedUntilRemoved() {
        final OrderBoardViewModel model = new OrderBoardViewModel();
        final AtomicInteger calls = new AtomicInteger();
        final OrderBoardViewModelListener listener = calls::incrementAndGet;
        model.addListener(listener);
        model.notifyStateChanged();
        assertEquals(1, calls.get());
        model.removeListener(listener);
        model.notifyStateChanged();
        assertEquals(1, calls.get());
    }

    @Test
    void stateKeepsReadOnlyTicketsAndMessages() {
        final OrderBoardState state = new OrderBoardState();
        assertTrue(state.getTickets().isEmpty());
        final List<OrderTicketRow> rows = new ArrayList<>(Collections.singletonList(
                new OrderTicketRow("o1", 7, Arrays.asList("2 × Burger"), "Placed", "PLACED", PLACED)));
        state.setTickets(rows);
        rows.clear();
        assertEquals(1, state.getTickets().size());
        assertThrows(UnsupportedOperationException.class, () -> state.getTickets().clear());

        state.fail("Order 7 is ready, so it can't be edited.");
        assertTrue(state.isError());
        state.succeed("Order 7 placed.");
        assertFalse(state.isError());
        assertEquals("Order 7 placed.", state.getMessage());

        final OrderBoardViewModel model = new OrderBoardViewModel();
        model.setState(state);
        assertEquals(state, model.getState());
        assertThrows(NullPointerException.class, () -> model.setState(null));
    }

    @Test
    void rowsStartUnrankedAndCanBeRanked() {
        final OrderTicketRow row = new OrderTicketRow("o1", 7, Arrays.asList("1 × Fries"), "In progress",
                "IN_PROGRESS", PLACED);
        assertEquals(0, row.getPriorityRank());
        assertEquals("", row.getPriorityReason());
        final OrderTicketRow ranked = row.withPriority(1, "waiting 12 min");
        assertEquals(1, ranked.getPriorityRank());
        assertEquals("waiting 12 min", ranked.getPriorityReason());
        assertEquals(7, ranked.getOrderNumber());
        assertEquals("In progress", ranked.getStatusLabel());
        assertEquals("IN_PROGRESS", ranked.getStatus());
        assertEquals(PLACED, ranked.getCreatedAt());
        assertEquals("o1", ranked.getOrderId());
        assertThrows(UnsupportedOperationException.class, () -> ranked.getItemLines().clear());
    }
}
