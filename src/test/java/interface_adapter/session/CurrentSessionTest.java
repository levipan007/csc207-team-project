package interface_adapter.session;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurrentSessionTest {
    @Test
    void startsLoggedOut() {
        final CurrentSession session = new CurrentSession();
        assertFalse(session.isLoggedIn());
        assertThrows(IllegalStateException.class, session::getTruckId);
        assertThrows(IllegalStateException.class, session::getOwnerId);
    }

    @Test
    void logInStoresTrimmedIdsAndCanSwitchTrucks() {
        final CurrentSession session = new CurrentSession();
        session.logIn(" owner-A ", " truck-A ");
        assertTrue(session.isLoggedIn());
        assertEquals("owner-A", session.getOwnerId());
        assertEquals("truck-A", session.getTruckId());
        session.logIn("owner-B", "truck-B");
        assertEquals("truck-B", session.getTruckId());
    }

    @Test
    void logOutClearsTheSession() {
        final CurrentSession session = new CurrentSession();
        session.logIn("owner-A", "truck-A");
        session.logOut();
        assertFalse(session.isLoggedIn());
        assertThrows(IllegalStateException.class, session::getTruckId);
    }

    @Test
    void blankIdsAreRejectedAndKeepTheCurrentSession() {
        final CurrentSession session = new CurrentSession();
        session.logIn("owner-A", "truck-A");
        assertThrows(IllegalArgumentException.class, () -> session.logIn(" ", "truck-B"));
        assertThrows(IllegalArgumentException.class, () -> session.logIn("owner-B", null));
        assertEquals("truck-A", session.getTruckId());
        assertEquals("owner-A", session.getOwnerId());
    }
}
