package interface_adapter.menu_management.view_sales_summary;

import interface_adapter.menu_management.sales_summary.SalesSummaryViewModel;
import use_case.menu_management.view_sales_summary.ViewSalesSummaryOutputBoundary;
import use_case.menu_management.view_sales_summary.ViewSalesSummaryOutputData;

/** Presents report results without talking to Swing or storage. */
public final class ViewSalesSummaryPresenter implements ViewSalesSummaryOutputBoundary {
    private final SalesSummaryViewModel viewModel;
    public ViewSalesSummaryPresenter(SalesSummaryViewModel viewModel) { this.viewModel = viewModel; }
    @Override
    public void prepareSuccessView(ViewSalesSummaryOutputData output) {
        viewModel.getState().success(output);
        viewModel.notifyStateChanged();
    }
    @Override
    public void prepareFailView(String error) {
        viewModel.getState().fail(error);
        viewModel.notifyStateChanged();
    }
}
