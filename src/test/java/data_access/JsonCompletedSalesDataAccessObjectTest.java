package data_access;

import entity.CompletedSale;
import entity.MenuItem;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.menu_management.menu.DataAccessException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonCompletedSalesDataAccessObjectTest {
    @TempDir
    Path directory;

    @Test
    void missingSalesAreEmptyAndSubsequentReadsSeeExternalCompletedSales() throws IOException {
        final JsonCompletedSalesDataAccessObject dao = new JsonCompletedSalesDataAccessObject(directory);
        assertTrue(dao.findCompletedSales("truck-a").isEmpty());
        writeSales(new JSONArray().put(sale("a", "truck-a")).put(sale("b", "truck-b")));
        final List<CompletedSale> sales = dao.findCompletedSales("truck-a");
        assertEquals(1, sales.size());
        assertEquals("a", sales.get(0).getId());
        assertEquals(1200, sales.get(0).getLines().get(0).getUnitPriceCents());
        sales.clear();
        assertEquals(1, new JsonCompletedSalesDataAccessObject(directory).findCompletedSales("truck-a").size());
        writeSales(new JSONArray().put(sale("a", "truck-a")).put(sale("c", "truck-a")));
        assertEquals(2, dao.findCompletedSales("truck-a").size());
    }

    @Test
    void menuPriceChangesAndArchiveNeverRewritePurchaseHistory() throws IOException {
        writeSales(new JSONArray().put(sale("a", "truck-a")));
        final String historyBefore = Files.readString(directory.resolve("completed-sales.json"));
        final JsonMenuDataAccessObject menus = new JsonMenuDataAccessObject(directory);
        menus.save(new MenuItem("burger", "truck-a", "Today's burger", "", 2000, "", true, false));
        menus.save(menus.get("burger").withDetails("Renamed burger", "", 2500, "").archive());
        final CompletedSale history = new JsonCompletedSalesDataAccessObject(directory)
                .findCompletedSales("truck-a").get(0);
        assertEquals("Burger bought earlier", history.getLines().get(0).getNameAtPurchase());
        assertEquals(2400, history.getLines().get(0).getLineTotalCents());
        assertEquals(historyBefore, Files.readString(directory.resolve("completed-sales.json")));
    }

    @Test
    void malformedAndDuplicateSalesFailInsteadOfBeingSkipped() throws IOException {
        final JsonCompletedSalesDataAccessObject dao = new JsonCompletedSalesDataAccessObject(directory);
        Files.writeString(directory.resolve("completed-sales.json"), "broken");
        assertThrows(DataAccessException.class, () -> dao.findCompletedSales("truck-a"));
        writeSales(new JSONArray().put(sale("same", "truck-a")).put(sale("same", "truck-b")));
        assertThrows(DataAccessException.class, () -> dao.findCompletedSales("truck-a"));
        final JSONObject badTime = sale("bad", "truck-b").put("completedAt", "not an instant");
        writeSales(new JSONArray().put(badTime));
        assertThrows(DataAccessException.class, () -> dao.findCompletedSales("truck-a"));
        final JSONObject badQuantity = sale("bad", "truck-a");
        badQuantity.getJSONArray("lines").getJSONObject(0).put("quantity", 0);
        writeSales(new JSONArray().put(badQuantity));
        assertThrows(DataAccessException.class, () -> dao.findCompletedSales("truck-a"));
    }

    @Test
    void aDirectoryInPlaceOfTheSalesFileIsAStorageFailure() throws IOException {
        Files.createDirectory(directory.resolve("completed-sales.json"));
        assertThrows(DataAccessException.class,
                () -> new JsonCompletedSalesDataAccessObject(directory).findCompletedSales("truck-a"));
    }

    private void writeSales(JSONArray sales) throws IOException {
        Files.writeString(directory.resolve("completed-sales.json"),
                new JSONObject().put("schemaVersion", 1).put("completedSales", sales).toString());
    }

    private static JSONObject sale(String id, String truck) {
        final JSONObject line = new JSONObject().put("itemId", "burger")
                .put("nameAtPurchase", "Burger bought earlier").put("unitPriceCents", 1200).put("quantity", 2);
        return new JSONObject().put("id", id).put("truckId", truck)
                .put("completedAt", "2026-11-20T17:00:00Z").put("lines", new JSONArray().put(line));
    }
}
