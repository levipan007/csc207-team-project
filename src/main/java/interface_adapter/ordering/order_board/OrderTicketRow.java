package interface_adapter.ordering.order_board;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * One order on the board, ready to display. It holds only strings, numbers and
 * an {@link Instant}, so views never need entity or use-case types.
 */
public final class OrderTicketRow {
    private final String orderId;
    private final int orderNumber;
    private final List<String> itemLines;
    private final String statusLabel;
    private final String status;
    private final Instant createdAt;
    private final int priorityRank;
    private final String priorityReason;

    /**
     * Creates a row.
     *
     * @param itemLines      formatted lines, for example "2 × Burger (no onions)"
     * @param statusLabel    readable status, for example "In progress"
     * @param status         the status name, for example "IN_PROGRESS", so views can pick colours
     * @param createdAt      when the order was placed, for the elapsed-time display
     * @param priorityRank   1 is cook first; 0 means not ranked
     * @param priorityReason short reason for the rank, or "" when not ranked
     */
    public OrderTicketRow(String orderId, int orderNumber, List<String> itemLines, String statusLabel,
                          String status, Instant createdAt, int priorityRank, String priorityReason) {
        this.orderId = Objects.requireNonNull(orderId);
        this.orderNumber = orderNumber;
        this.itemLines = Collections.unmodifiableList(new ArrayList<>(itemLines));
        this.statusLabel = Objects.requireNonNull(statusLabel);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.priorityRank = priorityRank;
        this.priorityReason = priorityReason == null ? "" : priorityReason;
    }

    /** Creates an unranked row. */
    public OrderTicketRow(String orderId, int orderNumber, List<String> itemLines, String statusLabel,
                          String status, Instant createdAt) {
        this(orderId, orderNumber, itemLines, statusLabel, status, createdAt, 0, "");
    }

    public String getOrderId() { return orderId; }
    public int getOrderNumber() { return orderNumber; }
    public List<String> getItemLines() { return itemLines; }
    public String getStatusLabel() { return statusLabel; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public int getPriorityRank() { return priorityRank; }
    public String getPriorityReason() { return priorityReason; }

    /** Returns this row with a priority rank and reason. */
    public OrderTicketRow withPriority(int rank, String reason) {
        return new OrderTicketRow(orderId, orderNumber, itemLines, statusLabel, status, createdAt, rank, reason);
    }
}
