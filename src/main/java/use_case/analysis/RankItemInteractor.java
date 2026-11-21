package use_case.analysis;
 
/** An item in an analysis ranking. */
public final class RankItemInteractor implements RankItemInputBoundary, SortRanking {
    private final RankItemDataAccessInterface dataAccess;
    private final RankItemOutputBoundary presenter;

    public RankItemInteractor(RankItemDataAccessInterface dataAccess, RankItemOutputBoundary presenter) {
        this.dataAccess = dataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(RankItemInputData inputData) {

    }
}
