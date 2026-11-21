package use_case.menu_management.menu;

import entity.MenuItem;
import java.util.Objects;

/** Immutable output values; presenters never receive a mutable domain object. */
public final class MenuItemData {
    private final String id;
    private final String truckId;
    private final String name;
    private final String description;
    private final long priceCents;
    private final String imageReference;
    private final boolean available;
    private final boolean archived;

    public MenuItemData(MenuItem item) {
        Objects.requireNonNull(item, "item");
        id = item.getId();
        truckId = item.getTruckId();
        name = item.getName();
        description = item.getDescription();
        priceCents = item.getPriceCents();
        imageReference = item.getImageReference();
        available = item.isAvailable();
        archived = item.isArchived();
    }

    public String getId() { return id; }
    public String getTruckId() { return truckId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public long getPriceCents() { return priceCents; }
    public String getImageReference() { return imageReference; }
    public boolean isAvailable() { return available; }
    public boolean isArchived() { return archived; }
}
