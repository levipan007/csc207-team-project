package use_case.menu;

import java.math.BigDecimal;

/** Shared validation for requests to the menu use cases. */
public final class MenuValidation {
    private MenuValidation() { }

    public static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return value.trim();
    }

    /** Parses a positive dollar amount without rounding or overflowing integer cents. */
    public static long parsePriceCents(String price) {
        String text = requireText(price, "Price");
        final BigDecimal amount;
        try {
            amount = new BigDecimal(text);
        }
        catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Enter a numeric price, for example 8.99.", exception);
        }
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("Price may have at most two decimal places.");
        }
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        try {
            return amount.movePointRight(2).longValueExact();
        }
        catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Price is too large.", exception);
        }
    }
}
