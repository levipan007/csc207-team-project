package app;

import data_access.JsonCompletedSalesDataAccessObject;
import data_access.JsonMenuDataAccessObject;
import interface_adapter.session.CurrentSession;
import java.awt.Dimension;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

/**
 * Builds the app: creates the shared {@link AppContext} and asks each area's
 * module to add its tabs. Feature wiring belongs in the modules, not here.
 */
public final class AppBuilder {
    private final Path dataDirectory;
    private final String truckId;
    private final Predicate<String> archiveConfirmation;

    public AppBuilder(Path dataDirectory, String truckId) {
        this(dataDirectory, truckId, null);
    }

    /** Supplies a confirmation policy for tests or an embedding application. */
    public AppBuilder(Path dataDirectory, String truckId, Predicate<String> archiveConfirmation) {
        this.dataDirectory = Objects.requireNonNull(dataDirectory, "A data directory is required.");
        if (truckId == null || truckId.trim().isEmpty()) {
            throw new IllegalArgumentException("A truck ID is required.");
        }
        this.truckId = truckId.trim();
        this.archiveConfirmation = archiveConfirmation;
    }

    /** Builds the screen independently of a JFrame, allowing headless integration tests. */
    public JTabbedPane buildPanel() {
        final CurrentSession session = new CurrentSession();
        // TODO(T3): the login screen replaces this development session.
        session.logIn("dev-owner", truckId);
        final AppContext context = new AppContext(dataDirectory, session,
                new JsonMenuDataAccessObject(dataDirectory),
                new JsonCompletedSalesDataAccessObject(dataDirectory));

        final OrderingModule ordering = new OrderingModule(context);
        final List<AppModule> modules = List.of(
                new MenuModule(context, archiveConfirmation),
                ordering,
                new CookingModule(context, ordering),
                new AnalysisModule(context, ordering),
                new AccountsModule(context));

        final JTabbedPane tabs = new JTabbedPane();
        for (AppModule module : modules) {
            module.addTabs(tabs);
        }
        return tabs;
    }

    public JFrame build() {
        final JFrame frame = new JFrame("TruckTrack");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setContentPane(buildPanel());
        frame.setMinimumSize(new Dimension(950, 660));
        frame.setSize(1180, 780);
        frame.setLocationRelativeTo(null);
        return frame;
    }
}
