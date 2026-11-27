package interface_adapter.menu_management.view_menu;

import interface_adapter.session.CurrentSession;
import use_case.menu_management.view_menu.ViewMenuInputBoundary;
import use_case.menu_management.view_menu.ViewMenuInputData;
import java.util.Objects;

/** Starts the ViewMenu use case with the truck in the current session. */
public final class ViewMenuController {
    private final ViewMenuInputBoundary inputBoundary;
    private final CurrentSession session;

    public ViewMenuController(ViewMenuInputBoundary inputBoundary, CurrentSession session) {
        this.inputBoundary = Objects.requireNonNull(inputBoundary);
        this.session = Objects.requireNonNull(session);
    }

    public void execute() {
        inputBoundary.execute(new ViewMenuInputData(session.getTruckId()));
    }
}
