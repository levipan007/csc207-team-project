package entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A customer's order and where it is in its lifecycle. The lifecycle uses the
 * State pattern:
 *
 * <pre>
 * PLACED ──► IN_PROGRESS ──► READY ──► COMPLETED
 *    │            │
 *    └────────────┴──► CANCELLED
 * </pre>
 *
 * An action that isn't allowed in the current status throws
 * {@link IllegalStateException} and leaves the order unchanged. Items can be
 * changed only while the order is placed or in progress.
 */
public final class Order {
    private final String id;
    private final String truckId;
    private final int orderNumber;
    private final List<OrderItem> items;
    private final Instant createdAt;
    private Instant readyAt;
    private Instant completedAt;
    private OrderState state;

    /** Creates a new order, which starts as {@link OrderStatus#PLACED}. */
    public Order(String id, String truckId, int orderNumber, List<OrderItem> items, Instant createdAt) {
        this(id, truckId, orderNumber, items, new PlacedState(), createdAt, null, null);
    }

    private Order(String id, String truckId, int orderNumber, List<OrderItem> items,
                  OrderState state, Instant createdAt, Instant readyAt, Instant completedAt) {
        if (id == null || id.trim().isEmpty() || truckId == null || truckId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID and truck ID are required.");
        }
        if (orderNumber < 1) {
            throw new IllegalArgumentException("Order number must be 1 or more.");
        }
        Objects.requireNonNull(items, "Order items are required.");
        if (items.isEmpty() || items.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("An order needs at least one item.");
        }
        this.id = id.trim();
        this.truckId = truckId.trim();
        this.orderNumber = orderNumber;
        this.items = new ArrayList<>();
        for (OrderItem item : items) {
            mergeInto(this.items, item);
        }
        this.state = state;
        this.createdAt = Objects.requireNonNull(createdAt, "Creation time is required.");
        this.readyAt = readyAt;
        this.completedAt = completedAt;
    }

    /**
     * Rebuilds a stored order, for data access objects only. The timestamps must
     * match the status: {@code readyAt} is set only once the order has been ready,
     * and {@code completedAt} only once it is completed.
     */
    public static Order restore(String id, String truckId, int orderNumber, List<OrderItem> items,
                                OrderStatus status, Instant createdAt, Instant readyAt, Instant completedAt) {
        Objects.requireNonNull(status, "Order status is required.");
        final boolean wasReady = status == OrderStatus.READY || status == OrderStatus.COMPLETED;
        if (wasReady != (readyAt != null)) {
            throw new IllegalArgumentException("Order " + orderNumber + " is " + OrderState.label(status)
                    + (wasReady ? " but has no ready time." : " but has a ready time."));
        }
        final boolean wasCompleted = status == OrderStatus.COMPLETED;
        if (wasCompleted != (completedAt != null)) {
            throw new IllegalArgumentException("Order " + orderNumber + " is " + OrderState.label(status)
                    + (wasCompleted ? " but has no completion time." : " but has a completion time."));
        }
        final Order order = new Order(id, truckId, orderNumber, items, OrderState.of(status),
                createdAt, readyAt, completedAt);
        if (readyAt != null) {
            requireNotBefore(readyAt, createdAt, "Ready time");
        }
        if (completedAt != null) {
            requireNotBefore(completedAt, readyAt, "Completion time");
        }
        return order;
    }

    public String getId() { return id; }
    public String getTruckId() { return truckId; }
    public int getOrderNumber() { return orderNumber; }
    public Instant getCreatedAt() { return createdAt; }

    /** Returns when the order became ready, or {@code null} if it hasn't been. */
    public Instant getReadyAt() { return readyAt; }

    /** Returns when the order was completed, or {@code null} if it hasn't been. */
    public Instant getCompletedAt() { return completedAt; }

    /** Returns a read-only copy of the order lines. */
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public OrderStatus getStatus() {
        return state.status();
    }

    /** True while items can still be changed: placed or in progress. */
    public boolean isEditable() {
        return state.isEditable();
    }

    /** Returns the sum of every line, in cents. */
    public long totalCents() {
        long total = 0;
        for (OrderItem item : items) {
            total = Math.addExact(total, item.subtotalCents());
        }
        return total;
    }

    /** Adds a line. A line for the same menu item with the same note is merged into it. */
    public void addItem(OrderItem item) {
        Objects.requireNonNull(item, "An order item is required.");
        requireEditable();
        final List<OrderItem> updated = new ArrayList<>(items);
        mergeInto(updated, item);
        replaceItems(updated);
    }

    /** Removes the line at {@code lineIndex}. The last line can't be removed; cancel instead. */
    public void removeItem(int lineIndex) {
        requireEditable();
        requireLine(lineIndex);
        if (items.size() == 1) {
            throw new IllegalStateException("An order needs at least one item. Cancel the order instead.");
        }
        items.remove(lineIndex);
    }

    /** Changes the quantity of the line at {@code lineIndex}. */
    public void setQuantity(int lineIndex, int quantity) {
        requireEditable();
        requireLine(lineIndex);
        items.set(lineIndex, items.get(lineIndex).withQuantity(quantity));
    }

    /** Changes the note on the line at {@code lineIndex}. */
    public void setNote(int lineIndex, String note) {
        requireEditable();
        requireLine(lineIndex);
        items.set(lineIndex, items.get(lineIndex).withNote(note));
    }

    /** PLACED to IN_PROGRESS. */
    public void startCooking() {
        state = state.startCooking();
    }

    /** IN_PROGRESS to READY, recording when it became ready. */
    public void markReady(Instant at) {
        Objects.requireNonNull(at, "Ready time is required.");
        final OrderState next = state.markReady();
        requireNotBefore(at, createdAt, "Ready time");
        state = next;
        readyAt = at;
    }

    /** READY to COMPLETED, recording when it was handed over. */
    public void complete(Instant at) {
        Objects.requireNonNull(at, "Completion time is required.");
        final OrderState next = state.complete();
        requireNotBefore(at, readyAt, "Completion time");
        state = next;
        completedAt = at;
    }

    /** PLACED or IN_PROGRESS to CANCELLED. */
    public void cancel() {
        state = state.cancel();
    }

    /** Returns the sales record for a completed order. Notes are not kept. */
    public CompletedSale toCompletedSale() {
        if (state.status() != OrderStatus.COMPLETED) {
            throw new IllegalStateException("Only a completed order can be recorded as a sale.");
        }
        final List<SaleLine> lines = new ArrayList<>();
        for (OrderItem item : items) {
            lines.add(item.toSaleLine());
        }
        return new CompletedSale(id, truckId, completedAt, lines);
    }

    private static void mergeInto(List<OrderItem> lines, OrderItem item) {
        for (int index = 0; index < lines.size(); index++) {
            final OrderItem existing = lines.get(index);
            if (existing.sameLineAs(item)) {
                lines.set(index, existing.withQuantity(Math.addExact(existing.getQuantity(), item.getQuantity())));
                return;
            }
        }
        lines.add(item);
    }

    private void replaceItems(List<OrderItem> updated) {
        items.clear();
        items.addAll(updated);
    }

    private void requireEditable() {
        if (!state.isEditable()) {
            throw new IllegalStateException("Order " + orderNumber + " is " + OrderState.label(state.status())
                    + ", so its items can't be changed. Only placed or in-progress orders can be edited.");
        }
    }

    private void requireLine(int lineIndex) {
        if (lineIndex < 0 || lineIndex >= items.size()) {
            throw new IllegalArgumentException("Order " + orderNumber + " has no line " + (lineIndex + 1) + ".");
        }
    }

    private static void requireNotBefore(Instant time, Instant earliest, String field) {
        if (time.isBefore(earliest)) {
            throw new IllegalArgumentException(field + " can't be before " + earliest + ".");
        }
    }
}
