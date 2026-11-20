package interface_adapter.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import use_case.menu.MenuItemData;

/** Display data for the menu screen; this is not the domain State pattern. */
public final class MenuState {
    private List<MenuItemData> items = Collections.emptyList();
    private String message = "Add your first menu item to get started.";
    private boolean error;
    private long successfulChange;
    private boolean closeEditor;

    public List<MenuItemData> getItems() { return items; }
    public String getMessage() { return message; }
    public boolean isError() { return error; }
    public long getSuccessfulChange() { return successfulChange; }
    public boolean shouldCloseEditor() { return closeEditor; }

    public void setItems(List<MenuItemData> value) {
        items = Collections.unmodifiableList(new ArrayList<>(value));
    }

    public void upsert(MenuItemData item) {
        List<MenuItemData> updated = new ArrayList<>(items);
        int index = -1;
        for (int i = 0; i < updated.size(); i++) {
            if (updated.get(i).getId().equals(item.getId())) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            updated.add(item);
        } else {
            updated.set(index, item);
        }
        setItems(updated);
    }

    public void success(String text, boolean finishEditing) {
        message = text;
        error = false;
        closeEditor = finishEditing;
        successfulChange++;
    }

    public void fail(String text) {
        message = text;
        error = true;
        closeEditor = false;
    }
}
