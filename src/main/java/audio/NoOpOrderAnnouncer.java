package audio;

import use_case.ordering.mark_ready.OrderAnnouncer;

/** Announces nothing. Used until the audio announcer exists, and for demos without sound. */
public final class NoOpOrderAnnouncer implements OrderAnnouncer {
    @Override
    public void announce(int orderNumber) {
        // Intentionally silent.
    }
}
