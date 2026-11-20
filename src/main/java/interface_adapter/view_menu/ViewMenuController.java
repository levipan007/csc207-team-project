package interface_adapter.view_menu;

import use_case.view_menu.ViewMenuInputBoundary;
import use_case.view_menu.ViewMenuInputData;

/** Starts the ViewMenu use case with the current truck context. */
public final class ViewMenuController {
    private final ViewMenuInputBoundary inputBoundary;
    private final String truckId;
    public ViewMenuController(ViewMenuInputBoundary inputBoundary, String truckId) {
        this.inputBoundary = inputBoundary;
        this.truckId = truckId;
    }
    public void execute() {
        inputBoundary.execute(new ViewMenuInputData(truckId));
    }
}
