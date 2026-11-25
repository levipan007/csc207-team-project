package interface_adapter.analysis;

import use_case.analysis.RankItemInputBoundary;
import use_case.analysis.RankItemInputData;
import use_case.analysis.SortRanking;

/** Passes rank item requests from the view to the use case. */
public final class RankItemController {
    private final RankItemInputBoundary interactor;

    public RankItemController(RankItemInputBoundary interactor) {
        this.interactor= interactor;
    }

    public void execute(String timePeriod, SortRanking sorting) {
        interactor.execute(new RankItemInputData(timePeriod, sorting));
    }
}
