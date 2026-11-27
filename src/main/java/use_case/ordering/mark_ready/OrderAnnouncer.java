package use_case.ordering.mark_ready;

/**
 * Announces that an order is ready for pickup. Implementations must return
 * quickly (do slow work on another thread) and must never throw: marking an
 * order ready has to succeed even when the announcement fails.
 */
public interface OrderAnnouncer {
    void announce(int orderNumber);
}
