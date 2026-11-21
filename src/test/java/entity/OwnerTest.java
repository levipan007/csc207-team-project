package entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OwnerTest {

    @Test
    void createOwnerWithValidInfo() {
        Owner owner = new Owner("o1", "talha", "secret123", "Talha", "talha@mail.com", "t1");
        assertEquals("o1", owner.getId());
        assertEquals("talha", owner.getUsername());
        assertEquals("secret123", owner.getPassword());
        assertEquals("Talha", owner.getDisplayName());
        assertEquals("talha@mail.com", owner.getEmail());
        assertEquals("t1", owner.getTruckId());
    }

    @Test
    void usernameIsSavedInLowerCase() {
        Owner owner = new Owner("o1", "  TalhaE ", "secret123", "Talha", "", "t1");
        assertEquals("talhae", owner.getUsername());
    }

    @Test
    void emailCanBeEmpty() {
        Owner owner = new Owner("o1", "talha", "secret123", "Talha", null, "t1");
        assertEquals("", owner.getEmail());
    }

    @Test
    void invalidUsernameThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Owner("o1", "ab", "secret123", "Talha", "", "t1"));
        assertThrows(IllegalArgumentException.class,
                () -> new Owner("o1", "talha efe", "secret123", "Talha", "", "t1"));
    }

    @Test
    void missingFieldsThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Owner("o1", "", "secret123", "Talha", "", "t1"));
        assertThrows(IllegalArgumentException.class,
                () -> new Owner("o1", "talha", "", "Talha", "", "t1"));
        assertThrows(IllegalArgumentException.class,
                () -> new Owner("o1", "talha", "secret123", " ", "", "t1"));
        assertThrows(IllegalArgumentException.class,
                () -> new Owner("o1", "talha", "secret123", "Talha", "", null));
    }

    @Test
    void invalidEmailThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Owner("o1", "talha", "secret123", "Talha", "talha.mail.com", "t1"));
    }
}
