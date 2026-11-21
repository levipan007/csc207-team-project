package use_case.analysis;

/** The sorting requested for a ranking. */
public final class RankItemInputData {
    private final SortRanking sorting;

    public RankItemInputData(SortRanking sorting) {
        this.sorting = sorting;
    }

    public SortRanking getSorting() { return sorting; }
}
