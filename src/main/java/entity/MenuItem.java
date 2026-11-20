package entity;

/** An immutable item in a food truck's menu. Prices are stored as integer cents. */
public final class MenuItem {
    private final String id;
    private final String truckId;
    private final String name;
    private final String description;
    private final long priceCents;
    private final String imageReference;
    private final boolean available;
    private final boolean archived;

    public MenuItem(String id, String truckId, String name, String description,
                    long priceCents, String imageReference, boolean available, boolean archived) {
        this.id = requireText(id, "Item ID");
        this.truckId = requireText(truckId, "Truck ID");
        this.name = requireText(name, "Item name");
        if (priceCents <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        if (archived && available) {
            throw new IllegalArgumentException("An archived item cannot be available.");
        }
        this.description = normalizeOptional(description);
        this.priceCents = priceCents;
        this.imageReference = normalizeOptional(imageReference);
        this.available = available;
        this.archived = archived;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        return value == null ? "" : value.trim();
    }

    public String getId() { return id; }
    public String getTruckId() { return truckId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public long getPriceCents() { return priceCents; }
    public String getImageReference() { return imageReference; }
    public boolean isAvailable() { return available; }
    public boolean isArchived() { return archived; }

    /** Returns revised details without changing this item or its identity. */
    public MenuItem withDetails(String newName, String newDescription,
                                long newPriceCents, String newImageReference) {
        requireActive();
        return new MenuItem(id, truckId, newName, newDescription, newPriceCents,
                newImageReference, available, false);
    }

    /** Returns an item with the requested temporary availability. */
    public MenuItem withAvailability(boolean newAvailability) {
        requireActive();
        return new MenuItem(id, truckId, name, description, priceCents,
                imageReference, newAvailability, false);
    }

    /** Keeps the historical record while removing it from future ordering. */
    public MenuItem archive() {
        if (archived) {
            return this;
        }
        return new MenuItem(id, truckId, name, description, priceCents,
                imageReference, false, true);
    }

    private void requireActive() {
        if (archived) {
            throw new IllegalStateException("Archived menu items cannot be changed.");
        }
    }
}
