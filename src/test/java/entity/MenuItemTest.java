package entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuItemTest {
    private MenuItem item() {
        return new MenuItem("item-1", "truck-1", "Burger", "Original", 899, "old.png", true, false);
    }

    @Test void editingCreatesANewItemWithoutChangingOriginalOrIdentity() {
        MenuItem original = item();
        MenuItem updated = original.withDetails("  Veggie Burger  ", " New description ", 999, "new.png");
        assertNotSame(original, updated);
        assertEquals(original.getId(), updated.getId());
        assertEquals(original.getTruckId(), updated.getTruckId());
        assertEquals("Veggie Burger", updated.getName());
        assertEquals("New description", updated.getDescription());
        assertEquals(999, updated.getPriceCents());
        assertEquals("new.png", updated.getImageReference());
        assertTrue(updated.isAvailable());
        assertFalse(updated.isArchived());
        assertEquals("Burger", original.getName());
        assertEquals(899, original.getPriceCents());
    }

    @Test void availabilityIsIndependentOfArchivingAndDetails() {
        MenuItem original = item();
        MenuItem soldOut = original.withAvailability(false);
        assertTrue(original.isAvailable());
        assertFalse(soldOut.isAvailable());
        assertFalse(soldOut.isArchived());
        assertEquals("Burger", soldOut.getName());
        assertTrue(soldOut.withAvailability(true).isAvailable());
        assertFalse(soldOut.withDetails("New name", "", 1000, "").isAvailable());
    }

    @Test void archiveRetainsHistoryAndIsIdempotent() {
        MenuItem original = item();
        MenuItem archived = original.archive();
        assertFalse(original.isArchived());
        assertTrue(archived.isArchived());
        assertFalse(archived.isAvailable());
        assertEquals(original.getId(), archived.getId());
        assertEquals(original.getName(), archived.getName());
        assertEquals(original.getPriceCents(), archived.getPriceCents());
        assertSame(archived, archived.archive());
        assertThrows(IllegalStateException.class, () -> archived.withAvailability(true));
        assertThrows(IllegalStateException.class, () -> archived.withDetails("New", "", 100, ""));
    }

    @ParameterizedTest @NullAndEmptySource @ValueSource(strings = {"   "})
    void requiredIdentityAndNameCannotBeBlank(String blank) {
        assertThrows(IllegalArgumentException.class,
                () -> new MenuItem(blank, "truck", "Item", "", 1, "", true, false));
        assertThrows(IllegalArgumentException.class,
                () -> new MenuItem("id", blank, "Item", "", 1, "", true, false));
        assertThrows(IllegalArgumentException.class,
                () -> new MenuItem("id", "truck", blank, "", 1, "", true, false));
    }

    @Test void rejectsInvalidPricesAndContradictoryArchiveState() {
        assertThrows(IllegalArgumentException.class,
                () -> new MenuItem("id", "truck", "Item", "", 0, "", true, false));
        assertThrows(IllegalArgumentException.class,
                () -> new MenuItem("id", "truck", "Item", "", -1, "", true, false));
        assertThrows(IllegalArgumentException.class,
                () -> new MenuItem("id", "truck", "Item", "", 1, "", true, true));
    }

    @Test void oldConstructorUsesDefaultCategoryAndNoPrepTime() {
        MenuItem item = item();
        assertEquals("Other", item.getCategory());
        assertEquals(0, item.getPrepTimeMinutes());
    }

    @Test void categoryIsTrimmedAndBlankBecomesOther() {
        assertEquals("Mains", new MenuItem("id", "t", "Item", "", 1, "", true, false, " Mains ", 5).getCategory());
        assertEquals("Other", new MenuItem("id", "t", "Item", "", 1, "", true, false, "  ", 5).getCategory());
        assertEquals("Other", new MenuItem("id", "t", "Item", "", 1, "", true, false, null, 5).getCategory());
    }

    @Test void prepTimeCannotBeNegative() {
        assertEquals(0, new MenuItem("id", "t", "Item", "", 1, "", true, false, "Mains", 0).getPrepTimeMinutes());
        assertThrows(IllegalArgumentException.class,
                () -> new MenuItem("id", "t", "Item", "", 1, "", true, false, "Mains", -1));
    }

    @Test void categoryAndPrepTimeSurviveEveryChange() {
        MenuItem original = new MenuItem("id", "t", "Item", "", 100, "", true, false, "Drinks", 3);
        MenuItem edited = original.withDetails("Renamed", "", 200, "");
        assertEquals("Drinks", edited.getCategory());
        assertEquals(3, edited.getPrepTimeMinutes());
        MenuItem soldOut = original.withAvailability(false);
        assertEquals("Drinks", soldOut.getCategory());
        assertEquals(3, soldOut.getPrepTimeMinutes());
        MenuItem archived = original.archive();
        assertEquals("Drinks", archived.getCategory());
        assertEquals(3, archived.getPrepTimeMinutes());
        MenuItem recategorized = original.withDetails("Item", "", 100, "", "Mains", 12);
        assertEquals("Mains", recategorized.getCategory());
        assertEquals(12, recategorized.getPrepTimeMinutes());
        assertEquals("Drinks", original.getCategory());
        assertThrows(IllegalStateException.class, () -> archived.withDetails("X", "", 1, "", "Mains", 1));
    }

    @Test void optionalDescriptionAndImageMayBeAbsent() {
        MenuItem item = new MenuItem(" id ", " truck ", " Item ", null, 1, null, false, false);
        assertEquals("id", item.getId());
        assertEquals("truck", item.getTruckId());
        assertEquals("", item.getDescription());
        assertEquals("", item.getImageReference());
    }
}
