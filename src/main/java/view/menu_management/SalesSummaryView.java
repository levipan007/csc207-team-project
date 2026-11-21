package view.menu_management;

import interface_adapter.menu_management.menu.MoneyFormatter;
import interface_adapter.menu_management.sales_summary.SalesSummaryState;
import interface_adapter.menu_management.sales_summary.SalesSummaryViewModel;
import interface_adapter.menu_management.sales_summary.SalesSummaryViewModelListener;
import interface_adapter.menu_management.view_sales_summary.ViewSalesSummaryController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import use_case.menu_management.view_sales_summary.TopItemData;
import use_case.menu_management.view_sales_summary.ViewSalesSummaryOutputData;

/** Date-range sales report screen. All calculations stay in the use-case layer. */
public final class SalesSummaryView extends JPanel implements SalesSummaryViewModelListener {
    private final SalesSummaryViewModel viewModel;
    private final ViewSalesSummaryController controller;
    private final JTextField from = new JTextField(11);
    private final JTextField to = new JTextField(11);
    private final JLabel revenue = new JLabel("—");
    private final JLabel orders = new JLabel("—");
    private final JLabel items = new JLabel("—");
    private final JTextArea message = new JTextArea(2, 30);
    private final SalesTableModel tableModel = new SalesTableModel();

    public SalesSummaryView(SalesSummaryViewModel viewModel, ViewSalesSummaryController controller) {
        super(new BorderLayout(14, 18));
        this.viewModel = viewModel;
        this.controller = controller;
        setBorder(BorderFactory.createEmptyBorder(22, 24, 20, 24));
        setBackground(new Color(247, 249, 250));
        JPanel top = new JPanel(new BorderLayout(0, 18));
        top.setOpaque(false);
        JLabel title = new JLabel("Sales summary");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 25f));
        JLabel subtitle = new JLabel("Completed sales · Canadian dollars · Toronto dates");
        subtitle.setForeground(new Color(76, 91, 103));
        JPanel headings = new JPanel(new BorderLayout(0, 6));
        headings.setOpaque(false);
        headings.add(title, BorderLayout.NORTH);
        headings.add(subtitle, BorderLayout.CENTER);
        top.add(headings, BorderLayout.NORTH);
        top.add(buildFilters(), BorderLayout.CENTER);
        JPanel metrics = new JPanel(new GridLayout(1, 3, 16, 0));
        metrics.setOpaque(false);
        revenue.setName("sales.revenue");
        orders.setName("sales.orders");
        items.setName("sales.items");
        metrics.add(metric("Sales revenue", revenue));
        metrics.add(metric("Completed orders", orders));
        metrics.add(metric("Items sold", items));
        top.add(metrics, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        JPanel centre = new JPanel(new BorderLayout(0, 10));
        centre.setOpaque(false);
        JLabel tableTitle = new JLabel("Items sold in this period");
        tableTitle.setFont(tableTitle.getFont().deriveFont(Font.BOLD, 16f));
        centre.add(tableTitle, BorderLayout.NORTH);
        JTable table = new JTable(tableModel);
        table.setName("sales.table");
        table.getAccessibleContext().setAccessibleName("Sales by menu item");
        table.setRowHeight(34);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane salesTable = new JScrollPane(table);
        salesTable.setColumnHeaderView(table.getTableHeader());
        centre.add(salesTable, BorderLayout.CENTER);
        add(centre, BorderLayout.CENTER);
        message.setName("sales.message");
        message.setEditable(false);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setPreferredSize(new Dimension(0, 44));
        message.setOpaque(false);
        message.getAccessibleContext().setAccessibleName("Sales report status");
        add(message, BorderLayout.SOUTH);
        viewModel.addListener(this);
        setCurrentMonth();
        render();
    }

    public void loadCurrentMonth() {
        setCurrentMonth();
        refresh();
    }

    public void refresh() { controller.execute(from.getText(), to.getText()); }

    private void setCurrentMonth() {
        LocalDate today = LocalDate.now(ZoneId.of("America/Toronto"));
        from.setText(today.withDayOfMonth(1).toString());
        to.setText(today.toString());
    }

    private JPanel buildFilters() {
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        from.setName("sales.from");
        to.setName("sales.to");
        from.setToolTipText("YYYY-MM-DD, inclusive");
        to.setToolTipText("YYYY-MM-DD, inclusive");
        JLabel startLabel = new JLabel("From");
        startLabel.setLabelFor(from);
        JLabel endLabel = new JLabel("To");
        endLabel.setLabelFor(to);
        JButton load = new JButton("Show summary");
        load.setName("sales.refresh");
        load.addActionListener(event -> refresh());
        JButton month = new JButton("This month");
        month.setName("sales.currentMonth");
        month.addActionListener(event -> loadCurrentMonth());
        from.addActionListener(event -> refresh());
        to.addActionListener(event -> refresh());
        filters.add(startLabel);
        filters.add(from);
        filters.add(endLabel);
        filters.add(to);
        filters.add(load);
        filters.add(month);
        return filters;
    }

    private JPanel metric(String title, JLabel value) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(221, 228, 233)),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        JLabel label = new JLabel(title);
        label.setForeground(new Color(76, 91, 103));
        value.setFont(value.getFont().deriveFont(Font.BOLD, 27f));
        value.getAccessibleContext().setAccessibleName(title);
        panel.add(label, BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        return panel;
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
        SalesSummaryState state = viewModel.getState();
        ViewSalesSummaryOutputData report = state.getReport();
        revenue.setText(report == null ? "—" : MoneyFormatter.format(report.getTotalRevenueCents()));
        orders.setText(report == null ? "—" : Integer.toString(report.getOrderCount()));
        items.setText(report == null ? "—" : Long.toString(report.getItemCount()));
        tableModel.setItems(report == null ? java.util.Collections.emptyList() : report.getTopItems());
        message.setText(state.getMessage());
        message.setForeground(state.isError() ? new Color(164, 35, 35) : new Color(76, 91, 103));
    }

    private static final class SalesTableModel extends AbstractTableModel {
        private final String[] columns = {"Item", "Quantity sold", "Revenue"};
        private List<TopItemData> rows = new ArrayList<>();
        void setItems(List<TopItemData> value) {
            rows = new ArrayList<>(value);
            fireTableDataChanged();
        }
        @Override
        public int getRowCount() { return rows.size(); }
        @Override
        public int getColumnCount() { return columns.length; }
        @Override
        public String getColumnName(int column) { return columns[column]; }
        @Override
        public Object getValueAt(int row, int column) {
            TopItemData item = rows.get(row);
            switch (column) {
                case 0: return item.getName();
                case 1: return item.getQuantity();
                default: return MoneyFormatter.format(item.getRevenueCents());
            }
        }
    }
}
