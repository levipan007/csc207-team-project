package entity;

/** A handed-over order. Final: nothing else is allowed. */
final class CompletedState implements OrderState {
    @Override
    public OrderStatus status() {
        return OrderStatus.COMPLETED;
    }
}
