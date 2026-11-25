package data_access.analysis;

import entity.CompletedSale;
import use_case.analysis.RankItemDataAccessInterface;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Data access for the rank items use case. */
public final class RankItemDataAccessObject implements RankItemDataAccessInterface {

    @Override
    public List<CompletedSale> getSalesData(LocalDateTime start, LocalDateTime end) {
        return new ArrayList<>();
    }
}
