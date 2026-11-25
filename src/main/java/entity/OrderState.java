package entity;

/**
 * State pattern for the order lifecycle. Each status is one class that allows
 * only its own transitions; every other action is rejected by these defaults.
 */
interface OrderState {
    OrderStatus status();

    default boolean isEditable() {
        return false;
    }

    default OrderState startCooking() {
        throw invalid("start cooking");
    }

    default OrderState markReady() {
        throw invalid("mark ready");
    }

    default OrderState complete() {
        throw invalid("finish");
    }

    default OrderState cancel() {
        throw invalid("cancel");
    }

    private IllegalStateException invalid(String action) {
        return new IllegalStateException("Cannot " + action + " an order that is "
                + label(status()) + ".");
    }

    /** Returns a readable status name, for example "in progress". */
    static String label(OrderStatus status) {
        return status.name().toLowerCase().replace('_', ' ');
    }

    /** Returns the state object for a stored status. */
    static OrderState of(OrderStatus status) {
        switch (status) {
            case PLACED:
                return new PlacedState();
            case IN_PROGRESS:
                return new InProgressState();
            case READY:
                return new ReadyState();
            case COMPLETED:
                return new CompletedState();
            case CANCELLED:
                return new CancelledState();
            default:
                throw new IllegalArgumentException("Unknown order status: " + status);
        }
    }
}
