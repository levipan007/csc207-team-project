package use_case.menu_management.view_sales_summary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Plain report data; the presenter decides how to display money and labels. */
public final class ViewSalesSummaryOutputData {
    private final long totalRevenueCents;
    private final int orderCount;
    private final long itemCount;
    private final List<TopItemData> topItems;

    public ViewSalesSummaryOutputData(long totalRevenueCents, int orderCount,
                                      long itemCount, List<TopItemData> topItems) {
        this.totalRevenueCents = totalRevenueCents;
        this.orderCount = orderCount;
        this.itemCount = itemCount;
        this.topItems = Collections.unmodifiableList(new ArrayList<>(topItems));
    }

    public long getTotalRevenueCents() { return totalRevenueCents; }
    public int getOrderCount() { return orderCount; }
    public long getItemCount() { return itemCount; }
    public List<TopItemData> getTopItems() { return topItems; }
}
