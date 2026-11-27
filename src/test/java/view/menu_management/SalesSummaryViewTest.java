package view.menu_management;

import interface_adapter.menu_management.sales_summary.SalesSummaryViewModel;
import interface_adapter.menu_management.view_sales_summary.ViewSalesSummaryController;
import interface_adapter.menu_management.view_sales_summary.ViewSalesSummaryPresenter;
import interface_adapter.session.CurrentSession;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import use_case.menu_management.view_sales_summary.TopItemData;
import use_case.menu_management.view_sales_summary.ViewSalesSummaryInputData;
import use_case.menu_management.view_sales_summary.ViewSalesSummaryOutputData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SalesSummaryViewTest {
    @Test
    void reportActionsDisplayCompletedSalesAndClearStaleTotalsAfterError() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            SalesSummaryViewModel model = new SalesSummaryViewModel();
            AtomicReference<ViewSalesSummaryInputData> received = new AtomicReference<>();
            CurrentSession session = new CurrentSession();
            session.logIn("owner-A", "truck-A");
            SalesSummaryView view = new SalesSummaryView(model,
                    new ViewSalesSummaryController(received::set, session, model));
            ((JTextField) MenuViewTest.find(view, "sales.from")).setText("2026-11-01");
            ((JTextField) MenuViewTest.find(view, "sales.to")).setText("2026-11-20");
            ((JButton) MenuViewTest.find(view, "sales.refresh")).doClick();
            assertEquals("truck-A", received.get().getTruckId());
            ViewSalesSummaryPresenter presenter = new ViewSalesSummaryPresenter(model);
            presenter.prepareSuccessView(new ViewSalesSummaryOutputData(1900, 1, 2,
                    Collections.singletonList(new TopItemData("item-1", "Burger", 2, 1900))));
            assertTrue(((JLabel) MenuViewTest.find(view, "sales.revenue")).getText().contains("19.00"));
            assertEquals("1", ((JLabel) MenuViewTest.find(view, "sales.orders")).getText());
            assertEquals("2", ((JLabel) MenuViewTest.find(view, "sales.items")).getText());
            assertEquals(1, ((JTable) MenuViewTest.find(view, "sales.table")).getRowCount());
            presenter.prepareFailView("Cannot load report.");
            assertEquals("—", ((JLabel) MenuViewTest.find(view, "sales.revenue")).getText());
            assertEquals(0, ((JTable) MenuViewTest.find(view, "sales.table")).getRowCount());
            received.set(null);
            view.loadCurrentMonth();
            assertNotNull(received.get());
            assertEquals(1, received.get().getStartDate().getDayOfMonth());
        });
    }
}
