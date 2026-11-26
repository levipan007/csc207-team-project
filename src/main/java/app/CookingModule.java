package app;

import java.awt.BorderLayout;
import java.util.Objects;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

/**
 * Wires the cooking screen (Talha). Adds the "Cooking" tab. Gets the order
 * board and ordering controllers from {@link OrderingModule}.
 */
public final class CookingModule implements AppModule {
    private final AppContext context;
    private final OrderingModule ordering;

    public CookingModule(AppContext context, OrderingModule ordering) {
        this.context = Objects.requireNonNull(context);
        this.ordering = Objects.requireNonNull(ordering);
    }

    @Override
    public void addTabs(JTabbedPane tabs) {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("The cooking screen is coming soon.", SwingConstants.CENTER), BorderLayout.CENTER);
        tabs.addTab("Cooking", panel);
    }
}
