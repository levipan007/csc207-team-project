package app;

import java.util.Objects;
import javax.swing.JTabbedPane;
import view.analysis.AnalyticsView;

/**
 * Wires analytics and order-priority recommendations (Heng). Adds the
 * "Analysis" tab. Gets the order board from {@link OrderingModule}.
 */
public final class AnalysisModule implements AppModule {
    private final AppContext context;
    private final OrderingModule ordering;

    public AnalysisModule(AppContext context, OrderingModule ordering) {
        this.context = Objects.requireNonNull(context);
        this.ordering = Objects.requireNonNull(ordering);
    }

    @Override
    public void addTabs(JTabbedPane tabs) {
        tabs.addTab("Analysis", new AnalyticsView());
    }
}
