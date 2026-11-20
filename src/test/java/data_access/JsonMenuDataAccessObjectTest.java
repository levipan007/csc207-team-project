package data_access;

import entity.MenuItem;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.menu.DataAccessException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonMenuDataAccessObjectTest {
    @TempDir
    Path directory;

    @Test
    void missingFileStartsEmptyAndRestartRetainsEditsAndArchive() {
        final JsonMenuDataAccessObject first = new JsonMenuDataAccessObject(directory);
        assertTrue(first.getAllForTruck("truck-a").isEmpty());
        assertNull(first.get("missing"));
        first.save(item("a", "truck-a", "Taco 🌮", 1250));
        final JsonMenuDataAccessObject restarted = new JsonMenuDataAccessObject(directory);
        assertEquals("Taco 🌮", restarted.get("a").getName());
        restarted.save(restarted.get("a").withDetails("New taco", "updated", 1500, "photo.png"));
        assertEquals(1500, first.get("a").getPriceCents());
        first.save(first.get("a").archive());
        final MenuItem archived = new JsonMenuDataAccessObject(directory).get("a");
        assertTrue(archived.isArchived());
        assertFalse(archived.isAvailable());
        assertEquals("updated", archived.getDescription());
        assertEquals("photo.png", archived.getImageReference());
    }

    @Test
    void listsAreIsolatedAndUpdatesCannotMoveItemsBetweenTrucks() {
        final JsonMenuDataAccessObject dao = new JsonMenuDataAccessObject(directory);
        dao.save(item("a", "truck-a", "A", 100));
        dao.save(item("b", "truck-b", "B", 200));
        final List<MenuItem> first = dao.getAllForTruck("truck-a");
        assertEquals(1, first.size());
        first.clear();
        assertEquals(1, dao.getAllForTruck("truck-a").size());
        assertEquals("b", dao.getAllForTruck("truck-b").get(0).getId());
        assertThrows(DataAccessException.class, () -> dao.save(item("a", "truck-b", "Hijack", 300)));
        assertEquals("truck-a", dao.get("a").getTruckId());
        dao.save(dao.get("a").withAvailability(false));
        assertFalse(new JsonMenuDataAccessObject(directory).get("a").isAvailable());
    }

    @Test
    void corruptFilesAreReportedAndNeverSilentlyReplaced() throws IOException {
        final Path file = directory.resolve("menu.json");
        final JsonMenuDataAccessObject dao = new JsonMenuDataAccessObject(directory);
        final String corrupt = "{ definitely invalid";
        Files.writeString(file, corrupt, StandardCharsets.UTF_8);
        assertThrows(DataAccessException.class, () -> dao.getAllForTruck("truck-a"));
        assertThrows(DataAccessException.class, () -> dao.save(item("a", "truck-a", "A", 100)));
        assertEquals(corrupt, Files.readString(file));

        Files.writeString(file, "");
        assertThrows(DataAccessException.class, () -> dao.get("a"));
        Files.writeString(file, "{\"schemaVersion\":2,\"items\":[]}");
        assertThrows(DataAccessException.class, () -> dao.get("a"));
        Files.writeString(file, "{\"schemaVersion\":1,\"items\":[]} trailing");
        assertThrows(DataAccessException.class, () -> dao.get("a"));
    }

    @Test
    void schemaRejectsWrongTypesDuplicateIdsAndInvalidArchiveState() throws IOException {
        final JsonMenuDataAccessObject dao = new JsonMenuDataAccessObject(directory);
        dao.save(item("a", "truck-a", "A", 100));
        final Path file = directory.resolve("menu.json");
        final String good = Files.readString(file);
        final Object[] badPrices = {"100", 10.5, -1, JSONObject.NULL};
        for (Object price : badPrices) {
            final JSONObject document = new JSONObject(good);
            document.getJSONArray("items").getJSONObject(0).put("priceCents", price);
            Files.writeString(file, document.toString());
            assertThrows(DataAccessException.class, () -> dao.get("a"));
        }
        final JSONObject duplicate = new JSONObject(good);
        duplicate.getJSONArray("items").put(duplicate.getJSONArray("items").getJSONObject(0));
        Files.writeString(file, duplicate.toString());
        assertThrows(DataAccessException.class, () -> dao.get("a"));
        final JSONObject wrongFlag = new JSONObject(good);
        wrongFlag.getJSONArray("items").getJSONObject(0).put("available", "true");
        Files.writeString(file, wrongFlag.toString());
        assertThrows(DataAccessException.class, () -> dao.get("a"));
        final JSONObject invalidArchive = new JSONObject(good);
        invalidArchive.getJSONArray("items").getJSONObject(0).put("archived", true);
        Files.writeString(file, invalidArchive.toString());
        assertThrows(DataAccessException.class, () -> dao.get("a"));
    }

    @Test
    void atomicWriteFailureLeavesExistingTargetAndCleansTemporaryFile() throws IOException {
        final Path target = directory.resolve("menu.json");
        Files.createDirectory(target);
        Files.writeString(target.resolve("keep.txt"), "do not replace");
        final DataAccessException exception = assertThrows(DataAccessException.class,
                () -> JsonFileStore.write(target, new JSONObject().put("schemaVersion", 1)
                        .put("items", new JSONArray())));
        assertTrue(exception.getMessage().contains("Cannot save"));
        assertEquals("do not replace", Files.readString(target.resolve("keep.txt")));
        try (java.util.stream.Stream<Path> entries = Files.list(directory)) {
            assertEquals(1, entries.count());
        }
        assertThrows(DataAccessException.class, () -> new JsonMenuDataAccessObject(directory).get("a"));
    }

    @Test
    void concurrentDaoInstancesDoNotLoseSeparateItemSaves() throws Exception {
        final JsonMenuDataAccessObject first = new JsonMenuDataAccessObject(directory);
        final JsonMenuDataAccessObject second = new JsonMenuDataAccessObject(directory);
        final ExecutorService workers = Executors.newFixedThreadPool(4);
        try {
            final List<Future<?>> tasks = new ArrayList<>();
            for (int index = 0; index < 24; index++) {
                final int number = index;
                tasks.add(workers.submit(() -> (number % 2 == 0 ? first : second)
                        .save(item("item-" + number, "truck-a", "Item " + number, number + 1))));
            }
            for (Future<?> task : tasks) {
                task.get();
            }
        }
        finally {
            workers.shutdownNow();
        }
        assertEquals(24, first.getAllForTruck("truck-a").size());
    }

    private static MenuItem item(String id, String truck, String name, long cents) {
        return new MenuItem(id, truck, name, "description", cents, "", true, false);
    }
}
