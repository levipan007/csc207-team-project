package use_case.view_sales_summary;

/** Starts the read-only sales summary use case. */
public interface ViewSalesSummaryInputBoundary {
    void execute(ViewSalesSummaryInputData inputData);
}
