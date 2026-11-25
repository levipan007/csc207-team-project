package entity;

/** A new order that hasn't been started. It can still be edited or cancelled. */
final class PlacedState implements OrderState {
    @Override
    public OrderStatus status() {
        return OrderStatus.PLACED;
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public OrderState startCooking() {
        return new InProgressState();
    }

    @Override
    public OrderState cancel() {
        return new CancelledState();
    }
}
