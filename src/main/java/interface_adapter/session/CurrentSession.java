package interface_adapter.session;

/**
 * The owner and truck currently using the app. Controllers read the truck when
 * they run, so logging in as another truck needs no rewiring.
 */
public final class CurrentSession {
    private String ownerId;
    private String truckId;

    /** Starts a session for this owner and truck, replacing any current one. */
    public void logIn(String newOwnerId, String newTruckId) {
        final String cleanOwner = requireText(newOwnerId, "Owner ID");
        final String cleanTruck = requireText(newTruckId, "Truck ID");
        ownerId = cleanOwner;
        truckId = cleanTruck;
    }

    /** Ends the session. */
    public void logOut() {
        ownerId = null;
        truckId = null;
    }

    public boolean isLoggedIn() {
        return truckId != null;
    }

    /** Returns the logged-in truck. Throws {@link IllegalStateException} if nobody is logged in. */
    public String getTruckId() {
        requireLoggedIn();
        return truckId;
    }

    /** Returns the logged-in owner. Throws {@link IllegalStateException} if nobody is logged in. */
    public String getOwnerId() {
        requireLoggedIn();
        return ownerId;
    }

    private void requireLoggedIn() {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Nobody is logged in. Log in to continue.");
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return value.trim();
    }
}
