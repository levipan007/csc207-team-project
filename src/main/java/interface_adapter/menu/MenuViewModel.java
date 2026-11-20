package interface_adapter.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Uses the custom listener convention of the course ca-login example. */
public final class MenuViewModel {
    private final List<MenuViewModelListener> listeners = new ArrayList<>();
    private MenuState state = new MenuState();

    public MenuState getState() { return state; }
    public void setState(MenuState value) { state = Objects.requireNonNull(value); }
    public void addListener(MenuViewModelListener listener) { listeners.add(listener); }
    public void removeListener(MenuViewModelListener listener) { listeners.remove(listener); }
    public void notifyStateChanged() {
        for (MenuViewModelListener listener : new ArrayList<>(listeners)) {
            listener.stateChanged();
        }
    }
}
