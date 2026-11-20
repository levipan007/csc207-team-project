package entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** An immutable completed order and the prices charged when it was purchased. */
public final class CompletedSale {
    private final String id;
    private final String truckId;
    private final Instant completedAt;
    private final List<SaleLine> lines;

    public CompletedSale(String id, String truckId, Instant completedAt, List<SaleLine> lines) {
        if (id == null || id.trim().isEmpty() || truckId == null || truckId.trim().isEmpty()) {
            throw new IllegalArgumentException("Sale ID and truck ID are required.");
        }
        this.completedAt = Objects.requireNonNull(completedAt, "Completion time is required.");
        Objects.requireNonNull(lines, "Sale lines are required.");
        if (lines.isEmpty() || lines.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("A completed sale needs at least one valid line.");
        }
        this.id = id;
        this.truckId = truckId;
        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
    }

    public String getId() { return id; }
    public String getTruckId() { return truckId; }
    public Instant getCompletedAt() { return completedAt; }
    public List<SaleLine> getLines() { return lines; }
}
