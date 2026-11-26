package use_case.menu_management.view_sales_summary;

import entity.CompletedSale;
import entity.SaleLine;
import use_case.shared.DataAccessException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Aggregates purchase snapshots without depending on the current menu. */
public final class ViewSalesSummaryInteractor implements ViewSalesSummaryInputBoundary {
    private final ViewSalesSummaryDataAccessInterface dataAccess;
    private final ViewSalesSummaryOutputBoundary presenter;
    private final ZoneId zoneId;

    public ViewSalesSummaryInteractor(ViewSalesSummaryDataAccessInterface dataAccess,
                                      ViewSalesSummaryOutputBoundary presenter) {
        this(dataAccess, presenter, ZoneId.of("America/Toronto"));
    }

    public ViewSalesSummaryInteractor(ViewSalesSummaryDataAccessInterface dataAccess,
                                      ViewSalesSummaryOutputBoundary presenter, ZoneId zoneId) {
        this.dataAccess = Objects.requireNonNull(dataAccess);
        this.presenter = Objects.requireNonNull(presenter);
        this.zoneId = Objects.requireNonNull(zoneId);
    }

    @Override
    public void execute(ViewSalesSummaryInputData inputData) {
        if (inputData == null || inputData.getTruckId() == null
                || inputData.getTruckId().trim().isEmpty()) {
            presenter.prepareFailView("Choose a truck before viewing sales.");
            return;
        }
        if (inputData.getStartDate() == null || inputData.getEndDate() == null
                || inputData.getStartDate().isAfter(inputData.getEndDate())) {
            presenter.prepareFailView("Enter a valid date range with the start on or before the end.");
            return;
        }
        final ViewSalesSummaryOutputData result;
        try {
            result = summarize(inputData);
        }
        catch (DataAccessException exception) {
            presenter.prepareFailView("Could not load completed sales: " + exception.getMessage());
            return;
        }
        catch (ArithmeticException exception) {
            presenter.prepareFailView("The sales totals are too large to display safely.");
            return;
        }
        presenter.prepareSuccessView(result);
    }

    private ViewSalesSummaryOutputData summarize(ViewSalesSummaryInputData inputData) {
        final List<CompletedSale> sales = new ArrayList<>(dataAccess.findCompletedSales(inputData.getTruckId()));
        sales.sort(Comparator.comparing(CompletedSale::getCompletedAt).thenComparing(CompletedSale::getId));
        final Map<String, TopItemData> totals = new HashMap<>();
        long revenue = 0;
        long itemCount = 0;
        int orderCount = 0;
        for (CompletedSale sale : sales) {
            final LocalDate date = sale.getCompletedAt().atZone(zoneId).toLocalDate();
            if (!inputData.getTruckId().equals(sale.getTruckId())
                    || date.isBefore(inputData.getStartDate()) || date.isAfter(inputData.getEndDate())) {
                continue;
            }
            orderCount = Math.incrementExact(orderCount);
            for (SaleLine line : sale.getLines()) {
                final long lineRevenue = line.getLineTotalCents();
                revenue = Math.addExact(revenue, lineRevenue);
                itemCount = Math.addExact(itemCount, line.getQuantity());
                final TopItemData previous = totals.get(line.getItemId());
                final long quantity = Math.addExact(previous == null ? 0 : previous.getQuantity(), line.getQuantity());
                final long itemRevenue = Math.addExact(previous == null ? 0 : previous.getRevenueCents(), lineRevenue);
                totals.put(line.getItemId(), new TopItemData(line.getItemId(), line.getNameAtPurchase(),
                        quantity, itemRevenue));
            }
        }
        final List<TopItemData> topItems = new ArrayList<>(totals.values());
        topItems.sort(Comparator.comparingLong(TopItemData::getQuantity).reversed()
                .thenComparing(Comparator.comparingLong(TopItemData::getRevenueCents).reversed())
                .thenComparing(TopItemData::getItemId));
        return new ViewSalesSummaryOutputData(revenue, orderCount, itemCount, topItems);
    }
}
