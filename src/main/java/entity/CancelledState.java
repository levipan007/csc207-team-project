package entity;

/** A cancelled order. */
final class CancelledState implements OrderState {
    @Override
    public OrderStatus status() {
        return OrderStatus.CANCELLED;
    }
}
