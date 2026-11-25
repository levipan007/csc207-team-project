package entity;

/** An immutable item in a food truck's menu. Prices are stored as integer cents. */
public final class MenuItem {
    /** The category used when none is given. */
    public static final String DEFAULT_CATEGORY = "Other";

    private final String id;
    private final String truckId;
    private final String name;
    private final String description;
    private final long priceCents;
    private final String imageReference;
    private final boolean available;
    private final boolean archived;
    private final String category;
    private final int prepTimeMinutes;

    /** Creates an item in the default category with no prep time recorded. */
    public MenuItem(String id, String truckId, String name, String description,
                    long priceCents, String imageReference, boolean available, boolean archived) {
        this(id, truckId, name, description, priceCents, imageReference, available, archived,
                DEFAULT_CATEGORY, 0);
    }

    /** Creates an item. A blank category becomes "Other"; prep time is whole minutes, 0 or more. */
    public MenuItem(String id, String truckId, String name, String description,
                    long priceCents, String imageReference, boolean available, boolean archived,
                    String category, int prepTimeMinutes) {
        this.id = requireText(id, "Item ID");
        this.truckId = requireText(truckId, "Truck ID");
        this.name = requireText(name, "Item name");
        if (priceCents <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        if (archived && available) {
            throw new IllegalArgumentException("An archived item cannot be available.");
        }
        if (prepTimeMinutes < 0) {
            throw new IllegalArgumentException("Prep time must be 0 minutes or more.");
        }
        this.description = normalizeOptional(description);
        this.priceCents = priceCents;
        this.imageReference = normalizeOptional(imageReference);
        this.available = available;
        this.archived = archived;
        final String cleanCategory = normalizeOptional(category);
        this.category = cleanCategory.isEmpty() ? DEFAULT_CATEGORY : cleanCategory;
        this.prepTimeMinutes = prepTimeMinutes;
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
    public String getCategory() { return category; }
    public int getPrepTimeMinutes() { return prepTimeMinutes; }

    /** Returns revised details, keeping this item's category and prep time. */
    public MenuItem withDetails(String newName, String newDescription,
                                long newPriceCents, String newImageReference) {
        return withDetails(newName, newDescription, newPriceCents, newImageReference,
                category, prepTimeMinutes);
    }

    /** Returns revised details, including category and prep time, without changing identity. */
    public MenuItem withDetails(String newName, String newDescription, long newPriceCents,
                                String newImageReference, String newCategory, int newPrepTimeMinutes) {
        requireActive();
        return new MenuItem(id, truckId, newName, newDescription, newPriceCents,
                newImageReference, available, false, newCategory, newPrepTimeMinutes);
    }

    /** Returns an item with the requested temporary availability. */
    public MenuItem withAvailability(boolean newAvailability) {
        requireActive();
        return new MenuItem(id, truckId, name, description, priceCents,
                imageReference, newAvailability, false, category, prepTimeMinutes);
    }

    /** Keeps the historical record while removing it from future ordering. */
    public MenuItem archive() {
        if (archived) {
            return this;
        }
        return new MenuItem(id, truckId, name, description, priceCents,
                imageReference, false, true, category, prepTimeMinutes);
    }

    private void requireActive() {
        if (archived) {
            throw new IllegalStateException("Archived menu items cannot be changed.");
        }
    }
}
