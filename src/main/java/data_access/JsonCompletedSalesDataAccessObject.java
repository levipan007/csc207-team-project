package data_access;

import entity.CompletedSale;
import entity.SaleLine;
import org.json.JSONArray;
import org.json.JSONObject;
import use_case.view_sales_summary.ViewSalesSummaryDataAccessInterface;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Reads completed-sales.json; order creation belongs to the ordering feature. */
public final class JsonCompletedSalesDataAccessObject implements ViewSalesSummaryDataAccessInterface {
    private final Path path;
    private final Object lock;

    public JsonCompletedSalesDataAccessObject(Path directory) {
        path = Objects.requireNonNull(directory).toAbsolutePath().normalize().resolve("completed-sales.json");
        lock = JsonFileStore.lockFor(path);
    }

    @Override
    public List<CompletedSale> findCompletedSales(String truckId) {
        synchronized (lock) {
            final JSONArray rows = JsonFileStore.read(path, "completedSales").getJSONArray("completedSales");
            final List<CompletedSale> result = new ArrayList<>();
            final Set<String> identifiers = new HashSet<>();
            try {
                for (int index = 0; index < rows.length(); index++) {
                    final JSONObject row = rows.getJSONObject(index);
                    final List<SaleLine> lines = new ArrayList<>();
                    final JSONArray lineRows = row.getJSONArray("lines");
                    for (int lineIndex = 0; lineIndex < lineRows.length(); lineIndex++) {
                        final JSONObject line = lineRows.getJSONObject(lineIndex);
                        lines.add(new SaleLine(JsonFileStore.string(line, "itemId"),
                                JsonFileStore.string(line, "nameAtPurchase"),
                                JsonFileStore.number(line, "unitPriceCents"),
                                Math.toIntExact(JsonFileStore.number(line, "quantity"))));
                    }
                    final CompletedSale sale = new CompletedSale(JsonFileStore.string(row, "id"),
                            JsonFileStore.string(row, "truckId"),
                            Instant.parse(JsonFileStore.string(row, "completedAt")), lines);
                    if (!identifiers.add(sale.getId())) {
                        throw new IllegalArgumentException("Duplicate completed sale ID.");
                    }
                    if (sale.getTruckId().equals(truckId)) {
                        result.add(sale);
                    }
                }
                return result;
            }
            catch (RuntimeException exception) {
                throw JsonFileStore.malformed(path, exception);
            }
        }
    }
}
