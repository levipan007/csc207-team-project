package entity;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompletedSaleTest {
    @Test
    void completedSaleRetainsImmutablePurchaseSnapshots() {
        final SaleLine line = new SaleLine("burger", "Burger at purchase", 1250, 2);
        final List<SaleLine> original = new ArrayList<>(Collections.singletonList(line));
        final Instant time = Instant.parse("2026-11-20T12:00:00Z");
        final CompletedSale sale = new CompletedSale("sale-1", "truck-1", time, original);
        original.clear();
        assertEquals("sale-1", sale.getId());
        assertEquals("truck-1", sale.getTruckId());
        assertEquals(time, sale.getCompletedAt());
        assertEquals(1, sale.getLines().size());
        assertEquals("burger", line.getItemId());
        assertEquals("Burger at purchase", line.getNameAtPurchase());
        assertEquals(1250, line.getUnitPriceCents());
        assertEquals(2, line.getQuantity());
        assertEquals(2500, line.getLineTotalCents());
        assertThrows(UnsupportedOperationException.class, () -> sale.getLines().clear());
    }

    @Test
    void totalsCoverOneLine() {
        final CompletedSale sale = new CompletedSale("s", "t", Instant.EPOCH,
                Collections.singletonList(new SaleLine("a", "A", 1250, 2)));
        assertEquals(2500, sale.totalCents());
        assertEquals(2, sale.totalUnits());
    }

    @Test
    void totalsAddEveryLine() {
        final CompletedSale sale = new CompletedSale("s", "t", Instant.EPOCH, Arrays.asList(
                new SaleLine("a", "A", 1250, 2), new SaleLine("b", "B", 300, 1), new SaleLine("c", "C", 0, 4)));
        assertEquals(2800, sale.totalCents());
        assertEquals(7, sale.totalUnits());
    }

    @Test
    void totalOverflowIsRejected() {
        final CompletedSale sale = new CompletedSale("s", "t", Instant.EPOCH, Arrays.asList(
                new SaleLine("a", "A", Long.MAX_VALUE, 1), new SaleLine("b", "B", 1, 1)));
        assertThrows(ArithmeticException.class, sale::totalCents);
    }

    @Test
    void invalidSalesCannotEnterTheReport() {
        final SaleLine valid = new SaleLine("a", "A", 0, 1);
        assertThrows(IllegalArgumentException.class, () -> new SaleLine(null, "A", 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new SaleLine(" ", "A", 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new SaleLine("a", null, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new SaleLine("a", " ", 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new SaleLine("a", "A", -1, 1));
        assertThrows(IllegalArgumentException.class, () -> new SaleLine("a", "A", 1, 0));
        assertThrows(ArithmeticException.class, () -> new SaleLine("a", "A", Long.MAX_VALUE, 2));
        assertThrows(IllegalArgumentException.class, () -> new CompletedSale(" ", "t", Instant.EPOCH,
                Collections.singletonList(valid)));
        assertThrows(IllegalArgumentException.class, () -> new CompletedSale(null, "t", Instant.EPOCH,
                Collections.singletonList(valid)));
        assertThrows(IllegalArgumentException.class, () -> new CompletedSale("s", null, Instant.EPOCH,
                Collections.singletonList(valid)));
        assertThrows(IllegalArgumentException.class, () -> new CompletedSale("s", " ", Instant.EPOCH,
                Collections.singletonList(valid)));
        assertThrows(NullPointerException.class, () -> new CompletedSale("s", "t", null,
                Collections.singletonList(valid)));
        assertThrows(NullPointerException.class, () -> new CompletedSale("s", "t", Instant.EPOCH, null));
        assertThrows(IllegalArgumentException.class, () -> new CompletedSale("s", "t", Instant.EPOCH,
                Collections.emptyList()));
        assertThrows(IllegalArgumentException.class, () -> new CompletedSale("s", "t", Instant.EPOCH,
                Arrays.asList(valid, null)));
    }
}
