package use_case.analysis;

/** The time period and sorting requested for a ranking. */
public final class RankItemInputData {
    private final String timePeriod;
    private final SortRanking sorting;

    public RankItemInputData(String timePeriod, SortRanking sorting) {
        this.timePeriod = timePeriod;
        this.sorting = sorting;
    }

    public String getTimePeriod() { return timePeriod; }
    public SortRanking getSorting() { return sorting; }
}
