package interface_adapter.menu_management.sales_summary;

import use_case.menu_management.view_sales_summary.ViewSalesSummaryOutputData;

/** Screen values for a requested date range and its completed-sales report. */
public final class SalesSummaryState {
    private ViewSalesSummaryOutputData report;
    private String range = "";
    private String message = "Choose a date range to view completed sales.";
    private boolean error;

    public ViewSalesSummaryOutputData getReport() { return report; }
    public String getRange() { return range; }
    public String getMessage() { return message; }
    public boolean isError() { return error; }
    public void setRange(String value) { range = value; }
    public void success(ViewSalesSummaryOutputData value) {
        report = value;
        error = false;
        message = value.getOrderCount() == 0 ? "No completed sales in this date range."
                : "Completed sales for " + range + ".";
    }
    public void fail(String value) {
        report = null;
        message = value;
        error = true;
    }
}
