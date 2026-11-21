package interface_adapter.menu_management.sales_summary;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Presentation state and notifications for the sales summary. */
public final class SalesSummaryViewModel {
    private SalesSummaryState state = new SalesSummaryState();
    private final List<SalesSummaryViewModelListener> listeners = new ArrayList<>();
    public SalesSummaryState getState() { return state; }
    public void setState(SalesSummaryState value) { state = Objects.requireNonNull(value); }
    public void addListener(SalesSummaryViewModelListener listener) { listeners.add(listener); }
    public void notifyStateChanged() {
        for (SalesSummaryViewModelListener listener : new ArrayList<>(listeners)) {
            listener.stateChanged();
        }
    }
}
