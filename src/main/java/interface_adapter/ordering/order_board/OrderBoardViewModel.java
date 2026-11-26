package interface_adapter.ordering.order_board;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The open orders shown on screen. One instance is shared by every presenter
 * that changes orders and every view that shows them.
 */
public final class OrderBoardViewModel {
    private final List<OrderBoardViewModelListener> listeners = new ArrayList<>();
    private OrderBoardState state = new OrderBoardState();

    public OrderBoardState getState() { return state; }
    public void setState(OrderBoardState value) { state = Objects.requireNonNull(value); }
    public void addListener(OrderBoardViewModelListener listener) { listeners.add(Objects.requireNonNull(listener)); }
    public void removeListener(OrderBoardViewModelListener listener) { listeners.remove(listener); }

    /** Tells every listener to redraw from the current state. */
    public void notifyStateChanged() {
        for (OrderBoardViewModelListener listener : new ArrayList<>(listeners)) {
            listener.stateChanged();
        }
    }
}
