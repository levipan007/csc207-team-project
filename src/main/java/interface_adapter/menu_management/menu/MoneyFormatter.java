package interface_adapter.menu_management.menu;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/** Formats integer cents without converting money to floating point. */
public final class MoneyFormatter {
    private MoneyFormatter() { }
    public static String format(long cents) {
        return NumberFormat.getCurrencyInstance(Locale.CANADA).format(BigDecimal.valueOf(cents, 2));
    }
    public static String input(long cents) { return BigDecimal.valueOf(cents, 2).toPlainString(); }
}
