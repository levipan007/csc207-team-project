package use_case.analysis;

import entity.CompletedSale;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
 
/** An item in an analysis ranking. */
public final class RankItemInteractor implements RankItemInputBoundary {
    private final RankItemDataAccessInterface dataAccess;
    private final RankItemOutputBoundary presenter;

    public RankItemInteractor(RankItemDataAccessInterface dataAccess, RankItemOutputBoundary presenter) {
        this.dataAccess = dataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(RankItemInputData inputData) {
        String timePeriod = inputData.getTimePeriod();
        SortRanking sorting = inputData.getSorting();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minus(Period.parse(timePeriod));
        List<CompletedSale> sales = dataAccess.getSalesData(startDate, now);
    }
}
