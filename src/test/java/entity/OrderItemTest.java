package entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemTest {
    @Test
    void keepsTrimmedSnapshotAndComputesSubtotal() {
        final OrderItem item = new OrderItem(" burger ", " Burger ", 1250, 3, " no onions ");
        assertEquals("burger", item.getMenuItemId());
        assertEquals("Burger", item.getNameAtOrder());
        assertEquals(1250, item.getUnitPriceCents());
        assertEquals(3, item.getQuantity());
        assertEquals("no onions", item.getNote());
        assertEquals(3750, item.subtotalCents());
    }

    @Test
    void missingNoteBecomesEmptyAndFreeItemsAreAllowed() {
        final OrderItem item = new OrderItem("water", "Water", 0, 1, null);
        assertEquals("", item.getNote());
        assertEquals(0, item.subtotalCents());
    }

    @Test
    void withQuantityAndWithNoteReturnNewLines() {
        final OrderItem item = new OrderItem("burger", "Burger", 1000, 1, "");
        final OrderItem more = item.withQuantity(4);
        final OrderItem noted = item.withNote("extra cheese");
        assertNotSame(item, more);
        assertEquals(1, item.getQuantity());
        assertEquals(4, more.getQuantity());
        assertEquals("extra cheese", noted.getNote());
        assertEquals("", item.getNote());
        assertThrows(IllegalArgumentException.class, () -> item.withQuantity(0));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void idAndNameAreRequired(String blank) {
        assertThrows(IllegalArgumentException.class, () -> new OrderItem(blank, "Burger", 1, 1, ""));
        assertThrows(IllegalArgumentException.class, () -> new OrderItem("burger", blank, 1, 1, ""));
    }

    @Test
    void rejectsNegativePriceBadQuantityAndOverflow() {
        assertThrows(IllegalArgumentException.class, () -> new OrderItem("a", "A", -1, 1, ""));
        assertThrows(IllegalArgumentException.class, () -> new OrderItem("a", "A", 1, 0, ""));
        assertThrows(IllegalArgumentException.class, () -> new OrderItem("a", "A", 1, -2, ""));
        assertThrows(ArithmeticException.class, () -> new OrderItem("a", "A", Long.MAX_VALUE, 2, ""));
    }
}
