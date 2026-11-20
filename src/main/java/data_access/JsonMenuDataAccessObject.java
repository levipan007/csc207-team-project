package data_access;

import entity.MenuItem;
import org.json.JSONArray;
import org.json.JSONObject;
import use_case.add_menu_item.AddMenuItemDataAccessInterface;
import use_case.archive_menu_item.ArchiveMenuItemDataAccessInterface;
import use_case.change_availability.ChangeAvailabilityDataAccessInterface;
import use_case.edit_menu_item.EditMenuItemDataAccessInterface;
import use_case.menu.DataAccessException;
import use_case.view_menu.ViewMenuDataAccessInterface;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Stores immutable menu snapshots in menu.json, re-reading on every operation. */
public final class JsonMenuDataAccessObject implements AddMenuItemDataAccessInterface,
        EditMenuItemDataAccessInterface, ArchiveMenuItemDataAccessInterface,
        ChangeAvailabilityDataAccessInterface, ViewMenuDataAccessInterface {
    private final Path path;
    private final Object lock;

    public JsonMenuDataAccessObject(Path directory) {
        path = Objects.requireNonNull(directory).toAbsolutePath().normalize().resolve("menu.json");
        lock = JsonFileStore.lockFor(path);
    }

    @Override
    public MenuItem get(String itemId) {
        synchronized (lock) {
            return readItems().get(itemId);
        }
    }

    @Override
    public List<MenuItem> getAllForTruck(String truckId) {
        synchronized (lock) {
            final List<MenuItem> result = new ArrayList<>();
            for (MenuItem item : readItems().values()) {
                if (item.getTruckId().equals(truckId)) {
                    result.add(item);
                }
            }
            return result;
        }
    }

    @Override
    public void save(MenuItem item) {
        Objects.requireNonNull(item, "A menu item is required.");
        synchronized (lock) {
            final Map<String, MenuItem> items = readItems();
            final MenuItem previous = items.get(item.getId());
            if (previous != null && !previous.getTruckId().equals(item.getTruckId())) {
                throw new DataAccessException("An item ID cannot be moved to another truck.");
            }
            items.put(item.getId(), item);
            final JSONArray rows = new JSONArray();
            for (MenuItem stored : items.values()) {
                rows.put(toJson(stored));
            }
            JsonFileStore.write(path, new JSONObject().put("schemaVersion", 1).put("items", rows));
        }
    }

    private Map<String, MenuItem> readItems() {
        final JSONArray rows = JsonFileStore.read(path, "items").getJSONArray("items");
        final Map<String, MenuItem> items = new LinkedHashMap<>();
        try {
            for (int index = 0; index < rows.length(); index++) {
                final JSONObject row = rows.getJSONObject(index);
                final MenuItem item = new MenuItem(JsonFileStore.string(row, "id"),
                        JsonFileStore.string(row, "truckId"), JsonFileStore.string(row, "name"),
                        JsonFileStore.string(row, "description"), JsonFileStore.number(row, "priceCents"),
                        JsonFileStore.string(row, "imageReference"), JsonFileStore.bool(row, "available"),
                        JsonFileStore.bool(row, "archived"));
                if (items.put(item.getId(), item) != null) {
                    throw new IllegalArgumentException("Duplicate menu item ID.");
                }
            }
            return items;
        }
        catch (RuntimeException exception) {
            throw JsonFileStore.malformed(path, exception);
        }
    }

    private static JSONObject toJson(MenuItem item) {
        return new JSONObject().put("id", item.getId()).put("truckId", item.getTruckId())
                .put("name", item.getName()).put("description", item.getDescription())
                .put("priceCents", item.getPriceCents()).put("imageReference", item.getImageReference())
                .put("available", item.isAvailable()).put("archived", item.isArchived());
    }
}
