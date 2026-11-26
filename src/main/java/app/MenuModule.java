package app;

import data_access.JsonMenuDataAccessObject;
import interface_adapter.menu_management.add_menu_item.AddMenuItemController;
import interface_adapter.menu_management.add_menu_item.AddMenuItemPresenter;
import interface_adapter.menu_management.archive_menu_item.ArchiveMenuItemController;
import interface_adapter.menu_management.archive_menu_item.ArchiveMenuItemPresenter;
import interface_adapter.menu_management.change_availability.ChangeAvailabilityController;
import interface_adapter.menu_management.change_availability.ChangeAvailabilityPresenter;
import interface_adapter.menu_management.edit_menu_item.EditMenuItemController;
import interface_adapter.menu_management.edit_menu_item.EditMenuItemPresenter;
import interface_adapter.menu_management.menu.MenuViewModel;
import interface_adapter.menu_management.sales_summary.SalesSummaryViewModel;
import interface_adapter.menu_management.view_menu.ViewMenuController;
import interface_adapter.menu_management.view_menu.ViewMenuPresenter;
import interface_adapter.menu_management.view_sales_summary.ViewSalesSummaryController;
import interface_adapter.menu_management.view_sales_summary.ViewSalesSummaryPresenter;
import interface_adapter.session.CurrentSession;
import use_case.menu_management.add_menu_item.AddMenuItemInteractor;
import use_case.menu_management.archive_menu_item.ArchiveMenuItemInteractor;
import use_case.menu_management.change_availability.ChangeAvailabilityInteractor;
import use_case.menu_management.edit_menu_item.EditMenuItemInteractor;
import use_case.menu_management.view_menu.ViewMenuInteractor;
import use_case.menu_management.view_sales_summary.ViewSalesSummaryInteractor;
import view.menu_management.MenuView;
import view.menu_management.SalesSummaryView;
import java.util.Objects;
import java.util.function.Predicate;
import javax.swing.JTabbedPane;

/** Wires menu management and the sales summary (Levi). Adds the "Menu" and "Sales summary" tabs. */
public final class MenuModule implements AppModule {
    private final AppContext context;
    private final Predicate<String> archiveConfirmation;

    /** @param archiveConfirmation asks before archiving; {@code null} shows the standard dialog */
    public MenuModule(AppContext context, Predicate<String> archiveConfirmation) {
        this.context = Objects.requireNonNull(context);
        this.archiveConfirmation = archiveConfirmation;
    }

    @Override
    public void addTabs(JTabbedPane tabs) {
        final CurrentSession session = context.getSession();
        final JsonMenuDataAccessObject menuDataAccess = context.getMenuDataAccess();
        final MenuViewModel menuViewModel = new MenuViewModel();
        final SalesSummaryViewModel salesViewModel = new SalesSummaryViewModel();

        final AddMenuItemController add = new AddMenuItemController(
                new AddMenuItemInteractor(menuDataAccess, new AddMenuItemPresenter(menuViewModel)), session);
        final EditMenuItemController edit = new EditMenuItemController(
                new EditMenuItemInteractor(menuDataAccess, new EditMenuItemPresenter(menuViewModel)), session);
        final ArchiveMenuItemController archive = new ArchiveMenuItemController(
                new ArchiveMenuItemInteractor(menuDataAccess, new ArchiveMenuItemPresenter(menuViewModel)), session);
        final ChangeAvailabilityController availability = new ChangeAvailabilityController(
                new ChangeAvailabilityInteractor(menuDataAccess,
                        new ChangeAvailabilityPresenter(menuViewModel)), session);
        final ViewMenuController list = new ViewMenuController(
                new ViewMenuInteractor(menuDataAccess, new ViewMenuPresenter(menuViewModel)), session);
        final MenuView menu = new MenuView(menuViewModel, add, edit, archive, availability, list,
                archiveConfirmation);

        final ViewSalesSummaryController summary = new ViewSalesSummaryController(
                new ViewSalesSummaryInteractor(context.getSalesDataAccess(),
                        new ViewSalesSummaryPresenter(salesViewModel)), session, salesViewModel);
        final SalesSummaryView sales = new SalesSummaryView(salesViewModel, summary);

        tabs.addTab("Menu", menu);
        tabs.addTab("Sales summary", sales);
        tabs.addChangeListener(event -> {
            if (tabs.getSelectedComponent() == sales) {
                sales.refresh();
            }
        });
        menu.refresh();
        sales.loadCurrentMonth();
    }
}
