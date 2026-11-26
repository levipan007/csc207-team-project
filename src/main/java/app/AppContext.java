package app;

import data_access.JsonCompletedSalesDataAccessObject;
import data_access.JsonMenuDataAccessObject;
import interface_adapter.session.CurrentSession;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Objects shared by more than one area, created once in {@link AppBuilder}.
 * Cloud storage would mean swapping the data access objects here; nothing else changes.
 */
public final class AppContext {
    private final Path dataDirectory;
    private final CurrentSession session;
    private final JsonMenuDataAccessObject menuDataAccess;
    private final JsonCompletedSalesDataAccessObject salesDataAccess;

    public AppContext(Path dataDirectory, CurrentSession session, JsonMenuDataAccessObject menuDataAccess,
                      JsonCompletedSalesDataAccessObject salesDataAccess) {
        this.dataDirectory = Objects.requireNonNull(dataDirectory, "A data directory is required.");
        this.session = Objects.requireNonNull(session, "A session is required.");
        this.menuDataAccess = Objects.requireNonNull(menuDataAccess, "The menu data access object is required.");
        this.salesDataAccess = Objects.requireNonNull(salesDataAccess, "The sales data access object is required.");
    }

    /** The folder every JSON data access object stores its file in. */
    public Path getDataDirectory() { return dataDirectory; }

    /** The logged-in owner and truck. Pass it to every controller. */
    public CurrentSession getSession() { return session; }

    /** Menu storage, used by menu management, ordering and analysis. */
    public JsonMenuDataAccessObject getMenuDataAccess() { return menuDataAccess; }

    /** Completed-sale storage, used by the sales summary, ordering and analysis. */
    public JsonCompletedSalesDataAccessObject getSalesDataAccess() { return salesDataAccess; }
}
