package use_case.analysis;

import entity.CompletedSale;
import java.time.LocalDateTime;
import java.util.List;

/** Data access needed by the rank items use case. */
public interface RankItemDataAccessInterface {
    /** Returns the completed sales made between start and end. */
    List<CompletedSale> getSalesData(LocalDateTime start, LocalDateTime end);
}
