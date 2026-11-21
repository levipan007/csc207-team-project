package use_case.menu_management.view_sales_summary;

import java.time.LocalDate;

/** The truck and inclusive local calendar dates requested by the user. */
public final class ViewSalesSummaryInputData {
    private final String truckId;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public ViewSalesSummaryInputData(String truckId, LocalDate startDate, LocalDate endDate) {
        this.truckId = truckId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getTruckId() { return truckId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
}
