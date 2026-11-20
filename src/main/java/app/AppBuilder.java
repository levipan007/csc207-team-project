package app;

import data_access.JsonCompletedSalesDataAccessObject;
import data_access.JsonMenuDataAccessObject;
import interface_adapter.add_menu_item.AddMenuItemController;
import interface_adapter.add_menu_item.AddMenuItemPresenter;
import interface_adapter.archive_menu_item.ArchiveMenuItemController;
import interface_adapter.archive_menu_item.ArchiveMenuItemPresenter;
import interface_adapter.change_availability.ChangeAvailabilityController;
import interface_adapter.change_availability.ChangeAvailabilityPresenter;
import interface_adapter.edit_menu_item.EditMenuItemController;
import interface_adapter.edit_menu_item.EditMenuItemPresenter;
import interface_adapter.menu.MenuViewModel;
import interface_adapter.sales_summary.SalesSummaryViewModel;
import interface_adapter.view_menu.ViewMenuController;
import interface_adapter.view_menu.ViewMenuPresenter;
import interface_adapter.view_sales_summary.ViewSalesSummaryController;
import interface_adapter.view_sales_summary.ViewSalesSummaryPresenter;
import use_case.add_menu_item.AddMenuItemInteractor;
import use_case.archive_menu_item.ArchiveMenuItemInteractor;
import use_case.change_availability.ChangeAvailabilityInteractor;
import use_case.edit_menu_item.EditMenuItemInteractor;
import use_case.view_menu.ViewMenuInteractor;
import use_case.view_sales_summary.ViewSalesSummaryInteractor;
import view.MenuView;
import view.SalesSummaryView;

import java.awt.Dimension;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Predicate;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

/** Connects Levi's use cases using the same assembly role as the course AppBuilder. */
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
        final JsonMenuDataAccessObject menuDataAccess = new JsonMenuDataAccessObject(dataDirectory);
        final JsonCompletedSalesDataAccessObject salesDataAccess =
                new JsonCompletedSalesDataAccessObject(dataDirectory);
        final MenuViewModel menuViewModel = new MenuViewModel();
        final SalesSummaryViewModel salesViewModel = new SalesSummaryViewModel();

        final AddMenuItemController add = new AddMenuItemController(
                new AddMenuItemInteractor(menuDataAccess, new AddMenuItemPresenter(menuViewModel)), truckId);
        final EditMenuItemController edit = new EditMenuItemController(
                new EditMenuItemInteractor(menuDataAccess, new EditMenuItemPresenter(menuViewModel)), truckId);
        final ArchiveMenuItemController archive = new ArchiveMenuItemController(
                new ArchiveMenuItemInteractor(menuDataAccess, new ArchiveMenuItemPresenter(menuViewModel)), truckId);
        final ChangeAvailabilityController availability = new ChangeAvailabilityController(
                new ChangeAvailabilityInteractor(menuDataAccess,
                        new ChangeAvailabilityPresenter(menuViewModel)), truckId);
        final ViewMenuController list = new ViewMenuController(
                new ViewMenuInteractor(menuDataAccess, new ViewMenuPresenter(menuViewModel)), truckId);
        final MenuView menu = new MenuView(menuViewModel, add, edit, archive, availability, list,
                archiveConfirmation);

        final ViewSalesSummaryController summary = new ViewSalesSummaryController(
                new ViewSalesSummaryInteractor(salesDataAccess,
                        new ViewSalesSummaryPresenter(salesViewModel)), truckId, salesViewModel);
        final SalesSummaryView sales = new SalesSummaryView(salesViewModel, summary);
        final JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Menu", menu);
        tabs.addTab("Sales summary", sales);
        tabs.addChangeListener(event -> {
            if (tabs.getSelectedComponent() == sales) {
                sales.refresh();
            }
        });
        menu.refresh();
        sales.loadCurrentMonth();
        return tabs;
    }

    public JFrame build() {
        final JFrame frame = new JFrame("TruckTrack — Menu & Sales");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setContentPane(buildPanel());
        frame.setMinimumSize(new Dimension(950, 660));
        frame.setSize(1180, 780);
        frame.setLocationRelativeTo(null);
        return frame;
    }
}
