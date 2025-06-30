package com.example.pos.view;

import com.example.pos.model.SaleRecord;
import com.example.pos.service.SalesReporter;
import com.example.pos.service.SalesReporter.SalesSummary;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SalesReportsPanel extends JPanel {

    private final SalesReporter salesReporter;
    private JComboBox<String> reportPeriodComboBox;
    private JTextArea summaryArea;
    private JTable salesDetailTable;
    private DefaultTableModel salesDetailTableModel;
    private JButton exportButton;

    private List<SaleRecord> currentReportSales; // Store the list of sales for the current report for export

    public SalesReportsPanel(SalesReporter salesReporter) {
        this.salesReporter = salesReporter;
        this.currentReportSales = List.of(); // Initialize to empty list
        initComponents();
        // Load default report (e.g., current day)
        loadReportData("Current Day");
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Top: Controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlsPanel.add(new JLabel("Report Period:"));
        String[] periods = {"Current Day", "Current Week", "Current Month", "All Sales"};
        reportPeriodComboBox = new JComboBox<>(periods);
        reportPeriodComboBox.addActionListener(e -> {
            String selectedPeriod = (String) reportPeriodComboBox.getSelectedItem();
            if (selectedPeriod != null) {
                loadReportData(selectedPeriod);
            }
        });
        controlsPanel.add(reportPeriodComboBox);

        exportButton = new JButton("Export Report (CSV)");
        exportButton.addActionListener(e -> exportReportToCSV());
        controlsPanel.add(exportButton);

        add(controlsPanel, BorderLayout.NORTH);

        // Center: Split pane for Summary and Details
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.3); // Give more space to details table initially

        // Summary Area
        summaryArea = new JTextArea(8, 40); // Rows, Columns
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane summaryScrollPane = new JScrollPane(summaryArea);
        summaryScrollPane.setBorder(BorderFactory.createTitledBorder("Summary"));
        splitPane.setTopComponent(summaryScrollPane);

        // Sales Detail Table
        String[] columnNames = {"Sale ID", "Timestamp", "Items", "Total", "Discount", "Final Amount"};
        salesDetailTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        salesDetailTable = new JTable(salesDetailTableModel);
        JScrollPane detailScrollPane = new JScrollPane(salesDetailTable);
        detailScrollPane.setBorder(BorderFactory.createTitledBorder("Details"));
        splitPane.setBottomComponent(detailScrollPane);

        add(splitPane, BorderLayout.CENTER);
    }

    private void loadReportData(String period) {
        switch (period) {
            case "Current Day":
                currentReportSales = salesReporter.getSalesForCurrentDay();
                break;
            case "Current Week":
                currentReportSales = salesReporter.getSalesForCurrentWeek();
                break;
            case "Current Month":
                currentReportSales = salesReporter.getSalesForCurrentMonth();
                break;
            case "All Sales":
            default:
                currentReportSales = salesReporter.getAllSales();
                break;
        }

        SalesSummary summary = salesReporter.generateSummary(currentReportSales);
        summaryArea.setText(summary.toString());

        salesDetailTableModel.setRowCount(0); // Clear existing rows
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (SaleRecord record : currentReportSales) {
            salesDetailTableModel.addRow(new Object[]{
                    record.getSaleId(),
                    record.getTimestamp().format(formatter),
                    record.getItems().size(),
                    record.getTotalAmount().toPlainString(),
                    record.getDiscountAmount().toPlainString(),
                    record.getFinalAmount().toPlainString()
            });
        }
         exportButton.setEnabled(!currentReportSales.isEmpty());
    }

    private void exportReportToCSV() {
        if (currentReportSales == null || currentReportSales.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No data to export.", "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report as CSV");
        fileChooser.setSelectedFile(new File("Sales_Report_" + reportPeriodComboBox.getSelectedItem().toString().replace(" ", "_") + ".csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // Ensure it has a .csv extension
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                // Write header
                writer.write("Sale ID,Timestamp,Item Count,Total Amount,Discount Amount,Final Amount,Cashier,Items (ID|Name|Qty|Price|Subtotal)\n");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                for (SaleRecord record : currentReportSales) {
                    StringBuilder line = new StringBuilder();
                    line.append(escapeCSV(record.getSaleId())).append(",");
                    line.append(escapeCSV(record.getTimestamp().format(formatter))).append(",");
                    line.append(record.getItems().size()).append(",");
                    line.append(record.getTotalAmount().toPlainString()).append(",");
                    line.append(record.getDiscountAmount().toPlainString()).append(",");
                    line.append(record.getFinalAmount().toPlainString()).append(",");
                    // Assuming SaleRecord might have a cashier user ID/name in future. For now, it's not stored directly in SaleRecord from file.
                    // We could fetch from current user if all sales are by them, or add to SaleRecord.
                    // For now, let's put a placeholder or leave blank.
                    line.append("N/A").append(","); // Placeholder for Cashier

                    // Item details - could be complex for CSV. Simple approach: Piped list of items.
                    // ProductID|ProductName|Qty|PriceAtSale|Subtotal;...
                    StringBuilder itemsStr = new StringBuilder();
                    record.getItems().forEach(item -> {
                        itemsStr.append(item.getProduct().getId()).append("|")
                                .append(escapeCSV(item.getProduct().getName())).append("|")
                                .append(item.getQuantity()).append("|")
                                .append(item.getPriceAtSale().toPlainString()).append("|")
                                .append(item.getSubtotal().toPlainString()).append(";");
                    });
                    if (itemsStr.length() > 0) itemsStr.deleteCharAt(itemsStr.length() - 1); // Remove last semicolon
                    line.append(escapeCSV(itemsStr.toString()));

                    writer.write(line.toString());
                    writer.newLine();
                }
                JOptionPane.showMessageDialog(this, "Report exported successfully to " + fileToSave.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error exporting report: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private String escapeCSV(String data) {
        if (data == null) return "";
        String escapedData = data.replace("\"", "\"\"");
        if (data.contains(",") || data.contains("\"") || data.contains("\n")) {
            escapedData = "\"" + escapedData + "\"";
        }
        return escapedData;
    }
}
