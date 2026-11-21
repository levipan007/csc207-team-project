package use_case.menu_management.view_sales_summary;

import entity.CompletedSale;
import java.util.List;

/** Supplies completed sales for a single truck without exposing storage details. */
public interface ViewSalesSummaryDataAccessInterface {
    List<CompletedSale> findCompletedSales(String truckId);
}
