package view.menu_management;

import interface_adapter.menu_management.add_menu_item.AddMenuItemController;
import interface_adapter.menu_management.archive_menu_item.ArchiveMenuItemController;
import interface_adapter.menu_management.change_availability.ChangeAvailabilityController;
import interface_adapter.menu_management.edit_menu_item.EditMenuItemController;
import interface_adapter.menu_management.menu.MenuState;
import interface_adapter.menu_management.menu.MenuViewModel;
import interface_adapter.menu_management.menu.MenuViewModelListener;
import interface_adapter.menu_management.menu.MoneyFormatter;
import interface_adapter.menu_management.view_menu.ViewMenuController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import use_case.menu_management.menu.MenuItemData;

/** Menu screen: forwards actions to controllers and displays view-model data. */
public final class MenuView extends JPanel implements MenuViewModelListener {
    private static final Color INK = new Color(29, 48, 62);
    private static final Color ERROR = new Color(164, 35, 35);
    private static final Color MUTED = new Color(76, 91, 103);
    private final MenuViewModel viewModel;
    private final AddMenuItemController addController;
    private final EditMenuItemController editController;
    private final ArchiveMenuItemController archiveController;
    private final ChangeAvailabilityController availabilityController;
    private final ViewMenuController viewController;
    private final Predicate<String> confirmArchive;
    private final MenuTableModel tableModel = new MenuTableModel();
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<MenuTableModel> sorter = new TableRowSorter<>(tableModel);
    private final JTextField search = new JTextField(20);
    private final JCheckBox showArchived = new JCheckBox("Include archived");
    private final JTextField name = new JTextField();
    private final JTextArea description = new JTextArea(3, 20);
    private final JTextField price = new JTextField();
    private final JTextField imageReference = new JTextField();
    private final JLabel preview = new JLabel("No image selected", SwingConstants.CENTER);
    private final JLabel editorTitle = new JLabel("Add menu item");
    private final JTextArea message = new JTextArea(2, 20);
    private final JButton editButton = button("Edit selected", "menu.edit");
    private final JButton availabilityButton = button("Mark sold out", "menu.availability");
    private final JButton archiveButton = button("Archive", "menu.archive");
    private final JButton saveButton = button("Add item", "menu.save");
    private String editingId;
    private long lastChange;

    public MenuView(MenuViewModel viewModel, AddMenuItemController addController,
                    EditMenuItemController editController, ArchiveMenuItemController archiveController,
                    ChangeAvailabilityController availabilityController, ViewMenuController viewController) {
        this(viewModel, addController, editController, archiveController, availabilityController,
                viewController, null);
    }

    /** The confirmation predicate makes archive interaction testable without a dialog. */
    public MenuView(MenuViewModel viewModel, AddMenuItemController addController,
                    EditMenuItemController editController, ArchiveMenuItemController archiveController,
                    ChangeAvailabilityController availabilityController, ViewMenuController viewController,
                    Predicate<String> archiveConfirmation) {
        super(new BorderLayout(18, 16));
        this.viewModel = viewModel;
        this.addController = addController;
        this.editController = editController;
        this.archiveController = archiveController;
        this.availabilityController = availabilityController;
        this.viewController = viewController;
        confirmArchive = archiveConfirmation == null ? this::askToArchive : archiveConfirmation;
        setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));
        setBackground(new Color(247, 249, 250));
        add(buildHeader(), BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildCatalogue(), buildEditor());
        split.setBorder(null);
        split.setResizeWeight(0.65);
        split.setContinuousLayout(true);
        add(split, BorderLayout.CENTER);
        message.setName("menu.message");
        message.setEditable(false);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setPreferredSize(new Dimension(0, 44));
        message.setOpaque(false);
        message.getAccessibleContext().setAccessibleName("Menu operation status");
        add(message, BorderLayout.SOUTH);
        viewModel.addListener(this);
        lastChange = viewModel.getState().getSuccessfulChange();
        render();
    }

    public void refresh() { viewController.execute(); }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 10));
        header.setOpaque(false);
        JLabel title = new JLabel("Your menu");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 25f));
        title.setForeground(INK);
        JLabel subtitle = new JLabel("Manage items, prices, images and what is available to order.");
        subtitle.setForeground(MUTED);
        JPanel titles = new JPanel(new BorderLayout(0, 5));
        titles.setOpaque(false);
        titles.add(title, BorderLayout.NORTH);
        titles.add(subtitle, BorderLayout.CENTER);
        header.add(titles, BorderLayout.CENTER);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);
        JButton refreshButton = button("Refresh", "menu.refresh");
        refreshButton.addActionListener(event -> refresh());
        JButton addButton = button("New item", "menu.new");
        addButton.addActionListener(event -> clearEditor());
        controls.add(refreshButton);
        controls.add(addButton);
        header.add(controls, BorderLayout.EAST);
        return header;
    }

    private JPanel buildCatalogue() {
        JPanel panel = new JPanel(new BorderLayout(8, 10));
        panel.setOpaque(false);
        panel.setMinimumSize(new Dimension(360, 400));
        JPanel filters = new JPanel(new BorderLayout(10, 0));
        filters.setOpaque(false);
        search.setName("menu.search");
        search.setToolTipText("Search item names or descriptions");
        search.getAccessibleContext().setAccessibleName("Search menu items");
        JLabel searchLabel = new JLabel("Search");
        searchLabel.setLabelFor(search);
        filters.add(searchLabel, BorderLayout.WEST);
        filters.add(search, BorderLayout.CENTER);
        showArchived.setName("menu.showArchived");
        showArchived.setOpaque(false);
        filters.add(showArchived, BorderLayout.EAST);
        search.getDocument().addDocumentListener(new ChangeListener(this::filterRows));
        showArchived.addActionListener(event -> filterRows());
        panel.add(filters, BorderLayout.NORTH);
        table.setName("menu.table");
        table.getAccessibleContext().setAccessibleName("Menu items");
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(34);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(228, 233, 237));
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setText(value == null ? "" : MoneyFormatter.format((Long) value));
            }
        });
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(210);
        table.getSelectionModel().addListSelectionListener(event -> updateActions());
        JScrollPane catalogue = new JScrollPane(table);
        catalogue.setColumnHeaderView(table.getTableHeader());
        panel.add(catalogue, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actions.setOpaque(false);
        editButton.addActionListener(event -> editSelected());
        availabilityButton.addActionListener(event -> changeSelectedAvailability());
        archiveButton.addActionListener(event -> archiveSelected());
        actions.add(editButton);
        actions.add(availabilityButton);
        actions.add(archiveButton);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildEditor() {
        JPanel panel = new FormPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        panel.setBackground(Color.WHITE);
        editorTitle.setFont(editorTitle.getFont().deriveFont(Font.BOLD, 18f));
        editorTitle.setForeground(INK);
        editorTitle.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(editorTitle);
        panel.add(Box.createVerticalStrut(16));
        name.setName("menu.name");
        price.setName("menu.price");
        description.setName("menu.description");
        imageReference.setName("menu.imageReference");
        addField(panel, "Item name *", name);
        addField(panel, "Price (CAD) *", price);
        price.setToolTipText("For example: 9.50");
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        addField(panel, "Description", new JScrollPane(description));
        imageReference.setEditable(false);
        addField(panel, "Local image (optional)", imageReference);
        JPanel imageButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        imageButtons.setOpaque(false);
        imageButtons.setAlignmentX(LEFT_ALIGNMENT);
        JButton chooseImage = button("Choose image…", "menu.chooseImage");
        chooseImage.addActionListener(event -> chooseImage());
        JButton clearImage = button("Remove image", "menu.clearImage");
        clearImage.addActionListener(event -> {
            imageReference.setText("");
            updateImagePreview();
        });
        imageButtons.add(chooseImage);
        imageButtons.add(clearImage);
        imageButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        panel.add(imageButtons);
        panel.add(Box.createVerticalStrut(10));
        preview.setName("menu.imagePreview");
        preview.setAlignmentX(LEFT_ALIGNMENT);
        preview.setPreferredSize(new Dimension(280, 125));
        preview.setMinimumSize(new Dimension(240, 100));
        preview.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
        preview.setOpaque(true);
        preview.setBackground(new Color(242, 245, 247));
        preview.setForeground(MUTED);
        preview.getAccessibleContext().setAccessibleName("Menu item image preview");
        panel.add(preview);
        panel.add(Box.createVerticalStrut(12));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        saveButton.addActionListener(event -> save());
        JButton cancel = button("Cancel editing", "menu.cancelEdit");
        cancel.addActionListener(event -> clearEditor());
        actions.add(saveButton);
        actions.add(cancel);
        actions.setBorder(BorderFactory.createEmptyBorder(8, 12, 10, 12));
        JPanel editor = new JPanel(new BorderLayout());
        editor.setName("menu.editor");
        editor.setBackground(Color.WHITE);
        editor.setBorder(BorderFactory.createLineBorder(new Color(221, 228, 233)));
        editor.setMinimumSize(new Dimension(300, 300));
        editor.setPreferredSize(new Dimension(350, 520));
        JScrollPane fields = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        fields.setName("menu.editorScroll");
        fields.setBorder(null);
        fields.getViewport().setBackground(Color.WHITE);
        fields.getVerticalScrollBar().setUnitIncrement(18);
        editor.add(fields, BorderLayout.CENTER);
        editor.add(actions, BorderLayout.SOUTH);
        return editor;
    }

    private void addField(JPanel panel, String labelText, javax.swing.JComponent component) {
        JLabel label = new JLabel(labelText);
        label.setAlignmentX(LEFT_ALIGNMENT);
        javax.swing.JComponent labelled = component;
        if (component instanceof JScrollPane
                && ((JScrollPane) component).getViewport().getView() instanceof javax.swing.JComponent) {
            labelled = (javax.swing.JComponent) ((JScrollPane) component).getViewport().getView();
        }
        label.setLabelFor(labelled);
        labelled.getAccessibleContext().setAccessibleName(labelText);
        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        component.setAlignmentX(LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                component instanceof JScrollPane ? 65 : 30));
        panel.add(component);
        panel.add(Box.createVerticalStrut(10));
    }

    private void save() {
        if (editingId == null) {
            addController.execute(name.getText(), description.getText(), price.getText(), imageReference.getText());
        } else {
            editController.execute(editingId, name.getText(), description.getText(),
                    price.getText(), imageReference.getText());
        }
    }

    private void editSelected() {
        MenuItemData item = selected();
        if (item != null && !item.isArchived()) {
            editingId = item.getId();
            editorTitle.setText("Edit menu item");
            saveButton.setText("Save changes");
            name.setText(item.getName());
            description.setText(item.getDescription());
            price.setText(MoneyFormatter.input(item.getPriceCents()));
            imageReference.setText(item.getImageReference());
            updateImagePreview();
            name.requestFocusInWindow();
        }
    }

    private void changeSelectedAvailability() {
        MenuItemData item = selected();
        if (item != null && !item.isArchived()) {
            availabilityController.execute(item.getId(), !item.isAvailable());
        }
    }

    private void archiveSelected() {
        MenuItemData item = selected();
        if (item != null && !item.isArchived() && confirmArchive.test(item.getName())) {
            archiveController.execute(item.getId());
        }
    }

    private boolean askToArchive(String itemName) {
        return JOptionPane.showConfirmDialog(this,
                "Archive “" + itemName + "”?\nIt will be removed from new orders. Existing orders stay unchanged.",
                "Archive menu item", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)
                == JOptionPane.OK_OPTION;
    }

    private void clearEditor() {
        editingId = null;
        editorTitle.setText("Add menu item");
        saveButton.setText("Add item");
        name.setText("");
        description.setText("");
        price.setText("");
        imageReference.setText("");
        updateImagePreview();
        name.requestFocusInWindow();
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a menu item image");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Images (PNG, JPG, GIF)", "png", "jpg", "jpeg", "gif"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            imageReference.setText(chooser.getSelectedFile().getAbsolutePath());
            updateImagePreview();
        }
    }

    private void updateImagePreview() {
        preview.setIcon(null);
        String reference = imageReference.getText();
        if (reference.isBlank()) {
            preview.setText("No image selected");
            return;
        }
        try {
            File file = new File(reference);
            if (file.length() > 10_000_000) {
                preview.setText("Image is too large to preview (10 MB limit)");
                return;
            }
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                preview.setText("Image preview unavailable");
                return;
            }
            double scale = Math.min(280.0 / image.getWidth(), 120.0 / image.getHeight());
            int width = Math.max(1, (int) (image.getWidth() * scale));
            int height = Math.max(1, (int) (image.getHeight() * scale));
            preview.setIcon(new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH)));
            preview.setText("");
        } catch (IOException | SecurityException exception) {
            preview.setText("Image unavailable — choose another local file");
        }
    }

    private MenuItemData selected() {
        int row = table.getSelectedRow();
        return row < 0 ? null : tableModel.getItem(table.convertRowIndexToModel(row));
    }

    private void updateActions() {
        MenuItemData item = selected();
        boolean editable = item != null && !item.isArchived();
        editButton.setEnabled(editable);
        archiveButton.setEnabled(editable);
        availabilityButton.setEnabled(editable);
        availabilityButton.setText(item != null && !item.isAvailable() ? "Make available" : "Mark sold out");
    }

    private void filterRows() {
        final String query = search.getText().trim().toLowerCase(Locale.ROOT);
        sorter.setRowFilter(new RowFilter<MenuTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends MenuTableModel, ? extends Integer> entry) {
                MenuItemData item = tableModel.getItem(entry.getIdentifier());
                return (showArchived.isSelected() || !item.isArchived())
                        && (item.getName() + " " + item.getDescription()).toLowerCase(Locale.ROOT).contains(query);
            }
        });
        updateActions();
    }

    @Override
    public void stateChanged() {
        if (SwingUtilities.isEventDispatchThread()) {
            render();
        } else {
            SwingUtilities.invokeLater(this::render);
        }
    }

    private void render() {
        MenuItemData selection = selected();
        String selectedId = selection == null ? null : selection.getId();
        MenuState state = viewModel.getState();
        tableModel.setItems(state.getItems());
        filterRows();
        if (selectedId != null) {
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                if (tableModel.getItem(row).getId().equals(selectedId)) {
                    int visibleRow = table.convertRowIndexToView(row);
                    if (visibleRow >= 0) { table.setRowSelectionInterval(visibleRow, visibleRow); }
                    break;
                }
            }
        }
        message.setText(state.getMessage());
        message.setForeground(state.isError() ? ERROR : MUTED);
        if (lastChange != state.getSuccessfulChange() && state.shouldCloseEditor()) { clearEditor(); }
        if (editingId != null && state.getItems().stream()
                .noneMatch(item -> item.getId().equals(editingId) && !item.isArchived())) {
            clearEditor();
        }
        lastChange = state.getSuccessfulChange();
        updateActions();
    }

    private static JButton button(String label, String componentName) {
        JButton result = new JButton(label);
        result.setName(componentName);
        return result;
    }

    /** Keep long forms readable while their action buttons stay visible below the scroll area. */
    private static final class FormPanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() { return new Dimension(330, 470); }
        @Override
        public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) { return 18; }
        @Override
        public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
            return Math.max(18, visible.height - 18);
        }
        @Override
        public boolean getScrollableTracksViewportWidth() { return true; }
        @Override
        public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private static final class ChangeListener implements DocumentListener {
        private final Runnable action;
        ChangeListener(Runnable action) { this.action = action; }
        @Override
        public void insertUpdate(DocumentEvent event) { action.run(); }
        @Override
        public void removeUpdate(DocumentEvent event) { action.run(); }
        @Override
        public void changedUpdate(DocumentEvent event) { action.run(); }
    }

    private static final class MenuTableModel extends AbstractTableModel {
        private final String[] columns = {"Item", "Price", "Availability", "Description"};
        private List<MenuItemData> items = new ArrayList<>();
        void setItems(List<MenuItemData> value) {
            items = new ArrayList<>(value);
            fireTableDataChanged();
        }
        MenuItemData getItem(int row) { return items.get(row); }
        @Override
        public int getRowCount() { return items.size(); }
        @Override
        public int getColumnCount() { return columns.length; }
        @Override
        public String getColumnName(int column) { return columns[column]; }
        @Override
        public Class<?> getColumnClass(int column) { return column == 1 ? Long.class : String.class; }
        @Override
        public Object getValueAt(int row, int column) {
            MenuItemData item = items.get(row);
            switch (column) {
                case 0: return item.getName();
                case 1: return item.getPriceCents();
                case 2: return item.isArchived() ? "Archived" : item.isAvailable() ? "Available" : "Sold out";
                default: return item.getDescription();
            }
        }
    }
}
