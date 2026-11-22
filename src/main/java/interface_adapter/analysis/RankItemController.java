package interface_adapter.analysis;

import use_case.analysis.RankItemInputBoundary;
import use_case.analysis.RankItemInputData;
import use_case.analysis.SortRanking;

/** Passes rank item requests from the view to the use case. */
public final class RankItemController {
    private final RankItemInputBoundary inputBoundary;

    public RankItemController(RankItemInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    public void execute(SortRanking sorting) {
        inputBoundary.execute(new RankItemInputData(sorting));
    }
}
