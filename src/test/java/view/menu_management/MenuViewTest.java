package view.menu_management;

import entity.MenuItem;
import interface_adapter.menu_management.add_menu_item.AddMenuItemController;
import interface_adapter.menu_management.add_menu_item.AddMenuItemPresenter;
import interface_adapter.menu_management.archive_menu_item.ArchiveMenuItemController;
import interface_adapter.menu_management.archive_menu_item.ArchiveMenuItemPresenter;
import interface_adapter.menu_management.change_availability.ChangeAvailabilityController;
import interface_adapter.menu_management.edit_menu_item.EditMenuItemController;
import interface_adapter.menu_management.edit_menu_item.EditMenuItemPresenter;
import interface_adapter.menu_management.menu.MenuViewModel;
import interface_adapter.menu_management.view_menu.ViewMenuController;
import interface_adapter.menu_management.view_menu.ViewMenuPresenter;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.menu_management.add_menu_item.AddMenuItemInputData;
import use_case.menu_management.add_menu_item.AddMenuItemOutputData;
import use_case.menu_management.archive_menu_item.ArchiveMenuItemInputData;
import use_case.menu_management.archive_menu_item.ArchiveMenuItemOutputData;
import use_case.menu_management.change_availability.ChangeAvailabilityInputData;
import use_case.menu_management.edit_menu_item.EditMenuItemInputData;
import use_case.menu_management.edit_menu_item.EditMenuItemOutputData;
import use_case.menu_management.menu.MenuItemData;
import use_case.menu_management.view_menu.ViewMenuOutputData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuViewTest {
    @TempDir
    Path imageDirectory;

    @Test
    void addingUsesControllerAndOnlyConfirmedSuccessClearsTheForm() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Harness h = new Harness(true);
            text(h.view, "menu.name").setText("Burger");
            text(h.view, "menu.price").setText("9.50");
            ((JTextArea) find(h.view, "menu.description")).setText("With lettuce");
            button(h.view, "menu.save").doClick();
            assertNotNull(h.add.get());
            assertEquals("truck-A", h.add.get().getTruckId());
            assertEquals("Burger", h.add.get().getName());
            assertEquals("With lettuce", h.add.get().getDescription());
            assertEquals(0, h.table().getRowCount());
            AddMenuItemPresenter presenter = new AddMenuItemPresenter(h.model);
            presenter.prepareFailView("Disk is full.");
            assertEquals("Burger", text(h.view, "menu.name").getText());
            assertEquals("Disk is full.", ((JTextArea) find(h.view, "menu.message")).getText());
            presenter.prepareSuccessView(new AddMenuItemOutputData(new MenuItemData(item())));
            assertEquals(1, h.table().getRowCount());
            assertEquals("", text(h.view, "menu.name").getText());
            h.view.refresh();
            assertEquals("truck-A", h.loadedTruck.get());
        });
    }

    @Test
    void editAvailabilityAndArchiveUseTheSelectedStableId() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Harness h = new Harness(true);
            h.list(item());
            h.table().setRowSelectionInterval(0, 0);
            button(h.view, "menu.edit").doClick();
            assertEquals("9.50", text(h.view, "menu.price").getText());
            text(h.view, "menu.name").setText("Updated burger");
            button(h.view, "menu.save").doClick();
            assertEquals("item-1", h.edit.get().getItemId());
            assertEquals("Updated burger", h.edit.get().getName());
            MenuItem updated = item().withDetails("Updated burger", "", 1200, "");
            new EditMenuItemPresenter(h.model).prepareSuccessView(
                    new EditMenuItemOutputData(new MenuItemData(updated)));
            assertEquals(1, h.table().getRowCount());
            assertEquals("Updated burger", h.table().getValueAt(0, 0));
            button(h.view, "menu.availability").doClick();
            assertEquals("item-1", h.availability.get().getItemId());
            assertFalse(h.availability.get().isAvailable());
            button(h.view, "menu.archive").doClick();
            assertEquals("item-1", h.archive.get().getItemId());
            new ArchiveMenuItemPresenter(h.model).prepareSuccessView(
                    new ArchiveMenuItemOutputData(new MenuItemData(updated.archive())));
            assertEquals(0, h.table().getRowCount());
            ((JCheckBox) find(h.view, "menu.showArchived")).doClick();
            assertEquals(1, h.table().getRowCount());
            h.table().setRowSelectionInterval(0, 0);
            assertFalse(button(h.view, "menu.edit").isEnabled());
            assertFalse(button(h.view, "menu.availability").isEnabled());
            assertFalse(button(h.view, "menu.archive").isEnabled());
        });
    }

    @Test
    void searchAndNumericPriceSortingKeepActionsAttachedToCorrectRow() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Harness h = new Harness(true);
            MenuItem expensive = new MenuItem("item-2", "truck-A", "Catering", "Party meal", 10000, "", true, false);
            new ViewMenuPresenter(h.model).prepareSuccessView(new ViewMenuOutputData(Arrays.asList(
                    new MenuItemData(expensive), new MenuItemData(item()))));
            h.table().getRowSorter().toggleSortOrder(1);
            assertEquals("Burger", h.table().getValueAt(0, 0));
            text(h.view, "menu.search").setText("party");
            assertEquals(1, h.table().getRowCount());
            h.table().setRowSelectionInterval(0, 0);
            button(h.view, "menu.availability").doClick();
            assertEquals("item-2", h.availability.get().getItemId());
            text(h.view, "menu.search").setText("nothing matches");
            assertEquals(0, h.table().getRowCount());
            assertFalse(button(h.view, "menu.edit").isEnabled());
        });
    }

    @Test
    void cancelledArchiveDoesNotInvokeUseCaseAndNewItemCancelsEditing() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Harness h = new Harness(false);
            h.list(item());
            h.table().setRowSelectionInterval(0, 0);
            button(h.view, "menu.archive").doClick();
            assertNull(h.archive.get());
            button(h.view, "menu.edit").doClick();
            assertEquals("Burger", text(h.view, "menu.name").getText());
            button(h.view, "menu.new").doClick();
            assertEquals("", text(h.view, "menu.name").getText());
            assertEquals("Add item", button(h.view, "menu.save").getText());
            assertTrue(button(h.view, "menu.save").isEnabled());
        });
    }

    @Test
    void savedImageReferenceReloadsAThumbnailAndMissingImageUsesPlaceholder() throws Exception {
        Path photo = imageDirectory.resolve("burger.png");
        ImageIO.write(new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB), "png", photo.toFile());
        SwingUtilities.invokeAndWait(() -> {
            Harness h = new Harness(true);
            text(h.view, "menu.name").setText("Burger");
            text(h.view, "menu.price").setText("9.50");
            text(h.view, "menu.imageReference").setText(photo.toString());
            button(h.view, "menu.save").doClick();
            assertEquals(photo.toString(), h.add.get().getImageReference());
            MenuItem saved = item().withDetails("Burger", "", 950, photo.toString());
            new AddMenuItemPresenter(h.model).prepareSuccessView(
                    new AddMenuItemOutputData(new MenuItemData(saved)));
            h.table().setRowSelectionInterval(0, 0);
            button(h.view, "menu.edit").doClick();
            JLabel preview = (JLabel) find(h.view, "menu.imagePreview");
            assertEquals(photo.toString(), text(h.view, "menu.imageReference").getText());
            assertNotNull(preview.getIcon());
            assertTrue(preview.getIcon().getIconWidth() <= 280);
            assertTrue(preview.getIcon().getIconHeight() <= 120);
            assertEquals("", preview.getText());

            h.list(saved.withDetails("Burger", "", 950, imageDirectory.resolve("missing.png").toString()));
            h.table().setRowSelectionInterval(0, 0);
            button(h.view, "menu.edit").doClick();
            assertNull(preview.getIcon());
            assertTrue(preview.getText().contains("unavailable"));
            button(h.view, "menu.clearImage").doClick();
            assertEquals("", text(h.view, "menu.imageReference").getText());
            assertEquals("No image selected", preview.getText());
        });
    }

    @Test
    void compactLayoutKeepsSaveActionsVisibleAndMakesTheFormScrollable() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Harness h = new Harness(true);
            h.view.setSize(920, 600);
            layout(h.view);
            JButton save = button(h.view, "menu.save");
            JButton cancel = button(h.view, "menu.cancelEdit");
            assertVisibleWithinParents(save, h.view);
            assertVisibleWithinParents(cancel, h.view);
            JScrollPane editor = (JScrollPane) find(h.view, "menu.editorScroll");
            assertTrue(editor.getVerticalScrollBar().isVisible());
            assertTrue(editor.getViewport().getViewSize().height > editor.getViewport().getExtentSize().height);
            assertEquals(h.table().getTableHeader(),
                    ((JScrollPane) h.table().getParent().getParent()).getColumnHeader().getView());
            JTextArea description = (JTextArea) find(h.view, "menu.description");
            assertEquals("Description", description.getAccessibleContext().getAccessibleName());
            assertTrue(hasLabelFor(h.view, "Description", description));
            assertEquals(44, find(h.view, "menu.message").getHeight());
        });
    }

    private static void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container) { layout((Container) child); }
        }
    }

    private static void assertVisibleWithinParents(Component component, Container root) {
        assertTrue(component.getWidth() > 0 && component.getHeight() > 0);
        Component child = component;
        while (child != root) {
            Container parent = child.getParent();
            assertNotNull(parent);
            assertTrue(new Rectangle(0, 0, parent.getWidth(), parent.getHeight()).contains(child.getBounds()),
                    child.getName() + " must remain inside its parent");
            child = parent;
        }
    }

    private static boolean hasLabelFor(Container root, String text, Component target) {
        for (Component child : root.getComponents()) {
            if (child instanceof JLabel && text.equals(((JLabel) child).getText())
                    && ((JLabel) child).getLabelFor() == target) { return true; }
            if (child instanceof Container && hasLabelFor((Container) child, text, target)) { return true; }
        }
        return false;
    }

    private static MenuItem item() {
        return new MenuItem("item-1", "truck-A", "Burger", "With lettuce", 950, "", true, false);
    }

    private static JTextField text(Container root, String name) { return (JTextField) find(root, name); }
    private static JButton button(Container root, String name) { return (JButton) find(root, name); }

    static Component find(Container root, String name) {
        for (Component child : root.getComponents()) {
            if (name.equals(child.getName())) { return child; }
            if (child instanceof Container) {
                Component result = find((Container) child, name);
                if (result != null) { return result; }
            }
        }
        return null;
    }

    private static final class Harness {
        private final MenuViewModel model = new MenuViewModel();
        private final AtomicReference<AddMenuItemInputData> add = new AtomicReference<>();
        private final AtomicReference<EditMenuItemInputData> edit = new AtomicReference<>();
        private final AtomicReference<ArchiveMenuItemInputData> archive = new AtomicReference<>();
        private final AtomicReference<ChangeAvailabilityInputData> availability = new AtomicReference<>();
        private final AtomicReference<String> loadedTruck = new AtomicReference<>();
        private final MenuView view;

        Harness(boolean archiveConfirmed) {
            view = new MenuView(model,
                    new AddMenuItemController(add::set, "truck-A"),
                    new EditMenuItemController(edit::set, "truck-A"),
                    new ArchiveMenuItemController(archive::set, "truck-A"),
                    new ChangeAvailabilityController(availability::set, "truck-A"),
                    new ViewMenuController(input -> loadedTruck.set(input.getTruckId()), "truck-A"),
                    name -> archiveConfirmed);
        }
        JTable table() { return (JTable) find(view, "menu.table"); }
        void list(MenuItem item) {
            new ViewMenuPresenter(model).prepareSuccessView(
                    new ViewMenuOutputData(Collections.singletonList(new MenuItemData(item))));
        }
    }
}
