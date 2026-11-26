package app;

import java.awt.BorderLayout;
import java.util.Objects;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

/** Wires accounts and the truck profile (Talha). Adds the "Settings" tab. */
public final class AccountsModule implements AppModule {
    private final AppContext context;

    public AccountsModule(AppContext context) {
        this.context = Objects.requireNonNull(context);
    }

    @Override
    public void addTabs(JTabbedPane tabs) {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Account settings are coming soon.", SwingConstants.CENTER), BorderLayout.CENTER);
        tabs.addTab("Settings", panel);
    }
}
