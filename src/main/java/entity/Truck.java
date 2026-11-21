package entity;

/**
 * A food truck. Each truck belongs to one owner.
 */
public class Truck {
    private final String id;
    private final String ownerId;
    private final String name;
    private final String cuisine;

    public Truck(String id, String ownerId, String name, String cuisine) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Truck ID is required.");
        }
        if (ownerId == null || ownerId.isBlank()) {
            throw new IllegalArgumentException("Owner ID is required.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Truck name is required.");
        }
        this.id = id.trim();
        this.ownerId = ownerId.trim();
        this.name = name.trim();
        this.cuisine = cuisine == null ? "" : cuisine.trim();
    }

    public String getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getCuisine() {
        return cuisine;
    }
}
