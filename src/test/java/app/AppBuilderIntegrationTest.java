package app;

import data_access.JsonMenuDataAccessObject;
import entity.MenuItem;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Exercises real screen actions through the assembled CA engines and JSON adapters. */
class AppBuilderIntegrationTest {
    @TempDir
    Path directory;

    @Test
    void screenActionsPersistReloadAndLeaveHistoricalRevenueUnchanged() throws Exception {
        Files.copy(Paths.get("docs", "fixtures", "completed-sales.example.json"),
                directory.resolve("completed-sales.json"));
        SwingUtilities.invokeAndWait(() -> {
            final JTabbedPane app = new AppBuilder(directory, "truck-1", ignored -> true).buildPanel();
            final Container menu = (Container) app.getComponentAt(0);
            final JTable table = find(menu, "menu.table", JTable.class);
            assertEquals(0, table.getRowCount());
            find(menu, "menu.name", JTextField.class).setText("Classic burger");
            find(menu, "menu.description", JTextArea.class).setText("Grilled patty, lettuce and house sauce");
            find(menu, "menu.price", JTextField.class).setText("9.50");
            find(menu, "menu.save", JButton.class).doClick();
            assertEquals(1, table.getRowCount());
            assertEquals(950L, table.getValueAt(0, 1));
            assertEquals("", find(menu, "menu.name", JTextField.class).getText());

            table.setRowSelectionInterval(0, 0);
            find(menu, "menu.edit", JButton.class).doClick();
            find(menu, "menu.price", JTextField.class).setText("-1");
            find(menu, "menu.save", JButton.class).doClick();
            assertEquals(950L, table.getValueAt(0, 1));
            assertEquals("-1", find(menu, "menu.price", JTextField.class).getText());
            find(menu, "menu.price", JTextField.class).setText("10.25");
            find(menu, "menu.save", JButton.class).doClick();
            assertEquals(1025L, table.getValueAt(0, 1));
            table.setRowSelectionInterval(0, 0);
            find(menu, "menu.availability", JButton.class).doClick();
            assertEquals("Sold out", table.getValueAt(0, 2));

            final JTabbedPane reopened = new AppBuilder(directory, "truck-1", ignored -> true).buildPanel();
            final Container reopenedMenu = (Container) reopened.getComponentAt(0);
            final JTable reopenedTable = find(reopenedMenu, "menu.table", JTable.class);
            assertEquals(1, reopenedTable.getRowCount());
            assertEquals(1025L, reopenedTable.getValueAt(0, 1));
            assertEquals("Sold out", reopenedTable.getValueAt(0, 2));
            reopenedTable.setRowSelectionInterval(0, 0);
            render(reopened, "menu.png");
            find(reopenedMenu, "menu.archive", JButton.class).doClick();
            assertEquals(0, reopenedTable.getRowCount());
            find(reopenedMenu, "menu.showArchived", JCheckBox.class).doClick();
            assertEquals("Archived", reopenedTable.getValueAt(0, 2));
            reopenedTable.setRowSelectionInterval(0, 0);
            assertFalse(find(reopenedMenu, "menu.edit", JButton.class).isEnabled());
            assertFalse(find(reopenedMenu, "menu.availability", JButton.class).isEnabled());
            final MenuItem saved = new JsonMenuDataAccessObject(directory).getAllForTruck("truck-1").get(0);
            assertTrue(saved.isArchived());
            assertFalse(saved.isAvailable());

            reopened.setSelectedIndex(1);
            final Container sales = (Container) reopened.getComponentAt(1);
            find(sales, "sales.from", JTextField.class).setText("2026-11-19");
            find(sales, "sales.to", JTextField.class).setText("2026-11-20");
            find(sales, "sales.refresh", JButton.class).doClick();
            assertEquals("$32.50", find(sales, "sales.revenue", JLabel.class).getText());
            assertEquals("2", find(sales, "sales.orders", JLabel.class).getText());
            assertEquals("4", find(sales, "sales.items", JLabel.class).getText());
            assertEquals(2, find(sales, "sales.table", JTable.class).getRowCount());
            render(reopened, "sales-summary.png");

            find(sales, "sales.from", JTextField.class).setText("2026-11-21");
            find(sales, "sales.refresh", JButton.class).doClick();
            assertEquals("—", find(sales, "sales.revenue", JLabel.class).getText());
            assertEquals(0, find(sales, "sales.table", JTable.class).getRowCount());
        });
    }

    @Test
    void everyAreaAddsItsTabsInOrder() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            final JTabbedPane app = new AppBuilder(directory, "truck-1").buildPanel();
            final String[] expected = {"Menu", "Sales summary", "New order", "Cooking", "Analysis", "Settings"};
            assertEquals(expected.length, app.getTabCount());
            for (int index = 0; index < expected.length; index++) {
                assertEquals(expected[index], app.getTitleAt(index));
            }
        });
    }

    @Test
    void standaloneTruckContextDoesNotListOrChangeAnotherTrucksMenu() throws Exception {
        new JsonMenuDataAccessObject(directory).save(
                new MenuItem("foreign", "truck-2", "Other truck's item", "", 500, "", true, false));
        SwingUtilities.invokeAndWait(() -> {
            final JTabbedPane app = new AppBuilder(directory, "truck-1").buildPanel();
            final Container menu = (Container) app.getComponentAt(0);
            assertEquals(0, find(menu, "menu.table", JTable.class).getRowCount());
        });
        assertEquals(1, new JsonMenuDataAccessObject(directory).getAllForTruck("truck-2").size());
    }

    private static <T extends Component> T find(Container root, String name, Class<T> type) {
        for (Component component : root.getComponents()) {
            if (name.equals(component.getName())) {
                return type.cast(component);
            }
            if (component instanceof Container) {
                final T result = findOrNull((Container) component, name, type);
                if (result != null) {
                    return result;
                }
            }
        }
        throw new AssertionError("Missing UI component: " + name);
    }

    private static <T extends Component> T findOrNull(Container root, String name, Class<T> type) {
        for (Component component : root.getComponents()) {
            if (name.equals(component.getName())) {
                return type.cast(component);
            }
            if (component instanceof Container) {
                final T result = findOrNull((Container) component, name, type);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static void render(JComponent panel, String filename) {
        try {
            panel.setSize(1180, 750);
            layout(panel);
            final BufferedImage image = new BufferedImage(1180, 750, BufferedImage.TYPE_INT_RGB);
            final Graphics2D graphics = image.createGraphics();
            panel.printAll(graphics);
            graphics.dispose();
            final Path output = Paths.get("target", "ui-preview");
            Files.createDirectories(output);
            ImageIO.write(image, "png", output.resolve(filename).toFile());
        }
        catch (Exception exception) {
            throw new AssertionError("Unable to render the assembled panel", exception);
        }
    }

    private static void layout(Container container) {
        container.doLayout();
        for (Component component : container.getComponents()) {
            if (component instanceof Container) {
                layout((Container) component);
            }
        }
    }
}
