package entity;

/**
 * A food truck owner's account.
 */
public class Owner {
    private final String id;
    private final String username;
    private final String password;
    private final String displayName;
    private final String email;
    private final String truckId;

    public Owner(String id, String username, String password, String displayName,
                 String email, String truckId) {
        checkNotBlank(id, "Owner ID");
        checkNotBlank(username, "Username");
        checkNotBlank(password, "Password");
        checkNotBlank(displayName, "Display name");
        checkNotBlank(truckId, "Truck ID");

        String cleanUsername = username.trim().toLowerCase();
        if (cleanUsername.length() < 3 || cleanUsername.contains(" ")) {
            throw new IllegalArgumentException("Username must be at least 3 characters with no spaces.");
        }

        String cleanEmail = email == null ? "" : email.trim();
        if (!cleanEmail.isEmpty() && !cleanEmail.contains("@")) {
            throw new IllegalArgumentException("Email is not valid.");
        }

        this.id = id.trim();
        this.username = cleanUsername;
        this.password = password;
        this.displayName = displayName.trim();
        this.email = cleanEmail;
        this.truckId = truckId.trim();
    }

    private static void checkNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getTruckId() {
        return truckId;
    }
}
