package view.analysis;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Placeholder screen for the analysis features. */
public final class AnalyticsView extends JPanel {
    public AnalyticsView() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        add(new JLabel("Analytics coming soon.", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}
