package app;

import interface_adapter.ordering.order_board.OrderBoardViewModel;
import java.awt.BorderLayout;
import java.util.Objects;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

/**
 * Wires ordering and the order lifecycle (Sophie). Adds the "New order" tab.
 * Owns the shared order board, and exposes the controllers other modules need
 * through getters.
 */
public final class OrderingModule implements AppModule {
    private final AppContext context;
    private final OrderBoardViewModel orderBoardViewModel = new OrderBoardViewModel();

    public OrderingModule(AppContext context) {
        this.context = Objects.requireNonNull(context);
    }

    /** The one order board shared by the ordering presenters, the cooking screen and the priority ranking. */
    public OrderBoardViewModel getOrderBoardViewModel() {
        return orderBoardViewModel;
    }

    @Override
    public void addTabs(JTabbedPane tabs) {
        tabs.addTab("New order", comingSoon("Taking orders is coming soon."));
    }

    private static JPanel comingSoon(String text) {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(text, SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }
}
