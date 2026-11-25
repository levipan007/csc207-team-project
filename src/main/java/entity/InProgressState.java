package entity;

/** An order being cooked. It can still be edited or cancelled. */
final class InProgressState implements OrderState {
    @Override
    public OrderStatus status() {
        return OrderStatus.IN_PROGRESS;
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public OrderState markReady() {
        return new ReadyState();
    }

    @Override
    public OrderState cancel() {
        return new CancelledState();
    }
}
