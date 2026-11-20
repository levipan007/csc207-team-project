package use_case.view_sales_summary;

/** Receives the summary or a user-facing explanation of failure. */
public interface ViewSalesSummaryOutputBoundary {
    void prepareSuccessView(ViewSalesSummaryOutputData outputData);
    void prepareFailView(String message);
}
