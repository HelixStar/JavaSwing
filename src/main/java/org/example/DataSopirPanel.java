package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataSopirPanel extends JPanel {
    private List<Object[]> allData;
    private DefaultTableModel model;
    private JTable table;
    private DatabaseManager dbManager;

    public DataSopirPanel() {
        setLayout(new BorderLayout());
        dbManager = DatabaseManager.getInstance();
        model = new DefaultTableModel(new String[]{
                "ID", "Nama Sopir", "Email", "Nomor Telepon", "Alamat", "Status Sopir", "Harga Sewa per Hari", "Created At"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disable direct editing in the table
            }
        };
        table = new JTable(model);

        fetchAndDisplayData();

        // Add MouseListener to detect double-click on a row
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {  // Double-click
                    int selectedRow = table.convertRowIndexToModel(table.getSelectedRow());
                    Object[] rowData = allData.get(selectedRow);
                    int sopirId = (int) rowData[0];  // Get sopir ID
                    new EditDriverDialog((JFrame) SwingUtilities.getWindowAncestor(DataSopirPanel.this),
                            sopirId, rowData, DataSopirPanel.this).setVisible(true);
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void fetchAndDisplayData() {
        allData = new ArrayList<>();
        try {
            ResultSet rs = dbManager.fetchSopirData();
            while (rs.next()) {
                allData.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nama_sopir"),
                        rs.getString("email"),
                        rs.getString("nomor_telepon"),
                        rs.getString("alamat"),
                        rs.getString("status_sopir"),
                        rs.getBigDecimal("harga_sewa_per_hari"),
                        rs.getTimestamp("created_at")
                });
            }

            updateTableData();
            System.out.println("Data sopir berhasil dimuat, jumlah data: " + allData.size());

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mengambil data sopir dari database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTableData() {
        model.setRowCount(0);
        for (Object[] rowData : allData) {
            model.addRow(rowData);
        }
    }

    public boolean hasData() {
        return allData != null && !allData.isEmpty();
    }

    // Method to refresh data after editing
    public void refreshData() {
        fetchAndDisplayData();
    }
}
