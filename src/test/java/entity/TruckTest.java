package entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TruckTest {

    @Test
    void createTruckWithValidInfo() {
        Truck truck = new Truck("t1", "o1", "Taco Time", "Mexican");
        assertEquals("t1", truck.getId());
        assertEquals("o1", truck.getOwnerId());
        assertEquals("Taco Time", truck.getName());
        assertEquals("Mexican", truck.getCuisine());
    }

    @Test
    void cuisineIsOptional() {
        Truck truck = new Truck("t1", "o1", "Taco Time", null);
        assertEquals("", truck.getCuisine());
    }

    @Test
    void missingNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Truck("t1", "o1", "", "Mexican"));
        assertThrows(IllegalArgumentException.class, () -> new Truck("t1", "o1", null, "Mexican"));
    }

    @Test
    void missingIdsThrowException() {
        assertThrows(IllegalArgumentException.class, () -> new Truck("", "o1", "Taco Time", ""));
        assertThrows(IllegalArgumentException.class, () -> new Truck("t1", " ", "Taco Time", ""));
    }
}
