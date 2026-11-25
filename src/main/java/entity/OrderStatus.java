package entity;

/** The stages of an order's lifecycle. See {@link Order} for the allowed transitions. */
public enum OrderStatus {
    PLACED,
    IN_PROGRESS,
    READY,
    COMPLETED,
    CANCELLED
}
