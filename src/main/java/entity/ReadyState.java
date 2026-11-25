package entity;

/** An order waiting for pickup. It can only be finished. */
final class ReadyState implements OrderState {
    @Override
    public OrderStatus status() {
        return OrderStatus.READY;
    }

    @Override
    public OrderState complete() {
        return new CompletedState();
    }
}
