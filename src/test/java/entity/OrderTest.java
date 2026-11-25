package entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderTest {
    private static final Instant CREATED = Instant.parse("2026-11-25T17:00:00Z");
    private static final Instant READY = Instant.parse("2026-11-25T17:10:00Z");
    private static final Instant DONE = Instant.parse("2026-11-25T17:12:00Z");

    private static OrderItem burger(int quantity, String note) {
        return new OrderItem("burger", "Burger", 1250, quantity, note);
    }

    private static OrderItem fries() {
        return new OrderItem("fries", "Fries", 400, 1, "");
    }

    private static Order newOrder() {
        return new Order("order-1", "truck-1", 7, Arrays.asList(burger(2, ""), fries()), CREATED);
    }

    private static Order orderIn(OrderStatus status) {
        final Order order = newOrder();
        switch (status) {
            case IN_PROGRESS:
                order.startCooking();
                break;
            case READY:
                order.startCooking();
                order.markReady(READY);
                break;
            case COMPLETED:
                order.startCooking();
                order.markReady(READY);
                order.complete(DONE);
                break;
            case CANCELLED:
                order.cancel();
                break;
            default:
                break;
        }
        return order;
    }

    @Test
    void newOrderStartsPlacedWithItsDetails() {
        final Order order = newOrder();
        assertEquals("order-1", order.getId());
        assertEquals("truck-1", order.getTruckId());
        assertEquals(7, order.getOrderNumber());
        assertEquals(CREATED, order.getCreatedAt());
        assertEquals(OrderStatus.PLACED, order.getStatus());
        assertTrue(order.isEditable());
        assertNull(order.getReadyAt());
        assertNull(order.getCompletedAt());
        assertEquals(2900, order.totalCents());
    }

    @Test
    void rejectsInvalidNewOrders() {
        final List<OrderItem> items = Collections.singletonList(fries());
        assertThrows(IllegalArgumentException.class, () -> new Order(" ", "t", 1, items, CREATED));
        assertThrows(IllegalArgumentException.class, () -> new Order("o", null, 1, items, CREATED));
        assertThrows(IllegalArgumentException.class, () -> new Order("o", "t", 0, items, CREATED));
        assertThrows(NullPointerException.class, () -> new Order("o", "t", 1, null, CREATED));
        assertThrows(IllegalArgumentException.class,
                () -> new Order("o", "t", 1, Collections.emptyList(), CREATED));
        assertThrows(IllegalArgumentException.class,
                () -> new Order("o", "t", 1, Arrays.asList(fries(), null), CREATED));
        assertThrows(NullPointerException.class, () -> new Order("o", "t", 1, items, null));
    }

    @Test
    void itemsAreCopiedAndReadOnly() {
        final List<OrderItem> items = new ArrayList<>(Collections.singletonList(fries()));
        final Order order = new Order("o", "t", 1, items, CREATED);
        items.clear();
        assertEquals(1, order.getItems().size());
        assertThrows(UnsupportedOperationException.class, () -> order.getItems().clear());
    }

    @Test
    void happyPathRecordsTimestampsAndBecomesASale() {
        final Order order = newOrder();
        order.startCooking();
        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        assertTrue(order.isEditable());
        order.markReady(READY);
        assertEquals(OrderStatus.READY, order.getStatus());
        assertEquals(READY, order.getReadyAt());
        assertFalse(order.isEditable());
        order.complete(DONE);
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertEquals(DONE, order.getCompletedAt());

        final CompletedSale sale = order.toCompletedSale();
        assertEquals("order-1", sale.getId());
        assertEquals("truck-1", sale.getTruckId());
        assertEquals(DONE, sale.getCompletedAt());
        assertEquals(order.totalCents(), sale.totalCents());
        assertEquals(3, sale.totalUnits());
        assertEquals("Burger", sale.getLines().get(0).getNameAtPurchase());
    }

    @Test
    void placedAndInProgressOrdersCanBeCancelled() {
        final Order placed = orderIn(OrderStatus.PLACED);
        placed.cancel();
        assertEquals(OrderStatus.CANCELLED, placed.getStatus());
        final Order cooking = orderIn(OrderStatus.IN_PROGRESS);
        cooking.cancel();
        assertEquals(OrderStatus.CANCELLED, cooking.getStatus());
        assertFalse(cooking.isEditable());
    }

    @Test
    void invalidTransitionsThrowAndChangeNothing() {
        assertInvalid(OrderStatus.PLACED, "mark ready", "complete");
        assertInvalid(OrderStatus.IN_PROGRESS, "start cooking", "complete");
        assertInvalid(OrderStatus.READY, "start cooking", "mark ready", "cancel");
        assertInvalid(OrderStatus.COMPLETED, "start cooking", "mark ready", "complete", "cancel");
        assertInvalid(OrderStatus.CANCELLED, "start cooking", "mark ready", "complete", "cancel");
    }

    private static void assertInvalid(OrderStatus status, String... actions) {
        for (String action : actions) {
            final Order order = orderIn(status);
            final Instant readyAt = order.getReadyAt();
            final Instant completedAt = order.getCompletedAt();
            final IllegalStateException error = assertThrows(IllegalStateException.class, () -> act(order, action));
            assertTrue(error.getMessage().contains(status.name().toLowerCase().replace('_', ' ')),
                    error.getMessage());
            assertEquals(status, order.getStatus());
            assertEquals(readyAt, order.getReadyAt());
            assertEquals(completedAt, order.getCompletedAt());
        }
    }

    private static void act(Order order, String action) {
        switch (action) {
            case "start cooking":
                order.startCooking();
                break;
            case "mark ready":
                order.markReady(Instant.parse("2026-11-25T18:00:00Z"));
                break;
            case "complete":
                order.complete(Instant.parse("2026-11-25T18:30:00Z"));
                break;
            default:
                order.cancel();
                break;
        }
    }

    @Test
    void timestampsCannotGoBackwards() {
        final Order cooking = orderIn(OrderStatus.IN_PROGRESS);
        assertThrows(IllegalArgumentException.class, () -> cooking.markReady(CREATED.minusSeconds(1)));
        assertEquals(OrderStatus.IN_PROGRESS, cooking.getStatus());
        assertNull(cooking.getReadyAt());
        assertThrows(NullPointerException.class, () -> cooking.markReady(null));

        final Order ready = orderIn(OrderStatus.READY);
        assertThrows(IllegalArgumentException.class, () -> ready.complete(READY.minusSeconds(1)));
        assertEquals(OrderStatus.READY, ready.getStatus());
        assertNull(ready.getCompletedAt());
        assertThrows(NullPointerException.class, () -> ready.complete(null));
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"PLACED", "IN_PROGRESS"})
    void editableOrdersCanChangeItems(OrderStatus status) {
        final Order order = orderIn(status);
        order.addItem(burger(1, ""));
        assertEquals(2, order.getItems().size());
        assertEquals(3, order.getItems().get(0).getQuantity());

        order.addItem(burger(1, "no onions"));
        assertEquals(3, order.getItems().size());

        order.setQuantity(1, 5);
        assertEquals(5, order.getItems().get(1).getQuantity());
        order.setNote(1, "extra salt");
        assertEquals("extra salt", order.getItems().get(1).getNote());

        order.removeItem(2);
        assertEquals(2, order.getItems().size());
        assertEquals(3 * 1250 + 5 * 400, order.totalCents());
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"READY", "COMPLETED", "CANCELLED"})
    void otherOrdersCannotChangeItems(OrderStatus status) {
        final Order order = orderIn(status);
        assertFalse(order.isEditable());
        assertThrows(IllegalStateException.class, () -> order.addItem(fries()));
        assertThrows(IllegalStateException.class, () -> order.removeItem(0));
        assertThrows(IllegalStateException.class, () -> order.setQuantity(0, 3));
        assertThrows(IllegalStateException.class, () -> order.setNote(0, "x"));
        assertEquals(2, order.getItems().size());
        assertEquals(2900, order.totalCents());
    }

    @Test
    void duplicateLinesMergeWhenCreated() {
        final Order order = new Order("o", "t", 1, Arrays.asList(burger(1, ""), burger(2, " "), fries()), CREATED);
        assertEquals(2, order.getItems().size());
        assertEquals(3, order.getItems().get(0).getQuantity());
    }

    @Test
    void lastLineCannotBeRemovedAndBadLinesAreRejected() {
        final Order order = new Order("o", "t", 1, Collections.singletonList(fries()), CREATED);
        final IllegalStateException error = assertThrows(IllegalStateException.class, () -> order.removeItem(0));
        assertTrue(error.getMessage().contains("Cancel the order"));
        assertThrows(IllegalArgumentException.class, () -> order.removeItem(1));
        assertThrows(IllegalArgumentException.class, () -> order.setQuantity(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> order.setNote(3, "x"));
        assertThrows(IllegalArgumentException.class, () -> order.setQuantity(0, 0));
        assertThrows(NullPointerException.class, () -> order.addItem(null));
        assertEquals(1, order.getItems().get(0).getQuantity());
    }

    @Test
    void onlyCompletedOrdersBecomeSales() {
        for (OrderStatus status : OrderStatus.values()) {
            if (status != OrderStatus.COMPLETED) {
                final Order order = orderIn(status);
                assertThrows(IllegalStateException.class, order::toCompletedSale);
            }
        }
    }

    @Test
    void restoreRebuildsEveryStatus() {
        final List<OrderItem> items = Collections.singletonList(fries());
        assertEquals(OrderStatus.PLACED,
                Order.restore("o", "t", 1, items, OrderStatus.PLACED, CREATED, null, null).getStatus());
        assertEquals(OrderStatus.IN_PROGRESS,
                Order.restore("o", "t", 1, items, OrderStatus.IN_PROGRESS, CREATED, null, null).getStatus());
        assertEquals(OrderStatus.CANCELLED,
                Order.restore("o", "t", 1, items, OrderStatus.CANCELLED, CREATED, null, null).getStatus());
        final Order ready = Order.restore("o", "t", 1, items, OrderStatus.READY, CREATED, READY, null);
        assertEquals(READY, ready.getReadyAt());
        ready.complete(DONE);
        final Order completed = Order.restore("o", "t", 1, items, OrderStatus.COMPLETED, CREATED, READY, DONE);
        assertEquals(DONE, completed.toCompletedSale().getCompletedAt());

        final Order restoredPlaced = Order.restore("o", "t", 1, items, OrderStatus.PLACED, CREATED, null, null);
        restoredPlaced.startCooking();
        assertEquals(OrderStatus.IN_PROGRESS, restoredPlaced.getStatus());
    }

    @Test
    void restoreRejectsTimestampsThatDontMatchTheStatus() {
        final List<OrderItem> items = Collections.singletonList(fries());
        assertThrows(NullPointerException.class,
                () -> Order.restore("o", "t", 1, items, null, CREATED, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> Order.restore("o", "t", 1, items, OrderStatus.PLACED, CREATED, READY, null));
        assertThrows(IllegalArgumentException.class,
                () -> Order.restore("o", "t", 1, items, OrderStatus.READY, CREATED, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> Order.restore("o", "t", 1, items, OrderStatus.READY, CREATED, READY, DONE));
        assertThrows(IllegalArgumentException.class,
                () -> Order.restore("o", "t", 1, items, OrderStatus.COMPLETED, CREATED, READY, null));
        assertThrows(IllegalArgumentException.class,
                () -> Order.restore("o", "t", 1, items, OrderStatus.COMPLETED, CREATED, null, DONE));
        assertThrows(IllegalArgumentException.class,
                () -> Order.restore("o", "t", 1, items, OrderStatus.CANCELLED, CREATED, null, DONE));
        assertThrows(IllegalArgumentException.class,
                () -> Order.restore("o", "t", 1, items, OrderStatus.READY, READY, CREATED, null));
        assertThrows(IllegalArgumentException.class,
                () -> Order.restore("o", "t", 1, items, OrderStatus.COMPLETED, CREATED, DONE, READY));
    }
}
