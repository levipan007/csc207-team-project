package interface_adapter.menu_management.view_sales_summary;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import interface_adapter.menu_management.sales_summary.SalesSummaryViewModel;
import use_case.menu_management.view_sales_summary.ViewSalesSummaryInputBoundary;
import use_case.menu_management.view_sales_summary.ViewSalesSummaryInputData;

/** Translates date fields to typed use-case input. */
public final class ViewSalesSummaryController {
    private final ViewSalesSummaryInputBoundary inputBoundary;
    private final String truckId;
    private final SalesSummaryViewModel viewModel;

    public ViewSalesSummaryController(ViewSalesSummaryInputBoundary inputBoundary,
                                      String truckId, SalesSummaryViewModel viewModel) {
        this.inputBoundary = inputBoundary;
        this.truckId = truckId;
        this.viewModel = viewModel;
    }

    public void execute(String startDate, String endDate) {
        final LocalDate start;
        final LocalDate end;
        try {
            start = LocalDate.parse(startDate == null ? "" : startDate.trim());
            end = LocalDate.parse(endDate == null ? "" : endDate.trim());
        } catch (DateTimeParseException exception) {
            viewModel.getState().fail("Enter valid dates using YYYY-MM-DD, for example 2026-11-20.");
            viewModel.notifyStateChanged();
            return;
        }
        viewModel.getState().setRange(start + " to " + end);
        inputBoundary.execute(new ViewSalesSummaryInputData(truckId, start, end));
    }
}
