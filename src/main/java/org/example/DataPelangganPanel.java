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

public class DataPelangganPanel extends JPanel {

    private List<Object[]> allData;
    private DefaultTableModel model;
    private JTable table;
    private DatabaseManager dbManager;

    public DataPelangganPanel() {
        setLayout(new BorderLayout());
        dbManager = DatabaseManager.getInstance();
        model = new DefaultTableModel(new String[]{"ID", "Nama Pelanggan", "Nomor Telepon", "Alamat", "Email", "Created At"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Nonaktifkan edit langsung di tabel
            }
        };
        table = new JTable(model);

        fetchAndDisplayData();

        // Tambahkan MouseListener untuk mendeteksi klik dua kali pada baris
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {  // Double-click
                    int selectedRow = table.convertRowIndexToModel(table.getSelectedRow());
                    Object[] rowData = allData.get(selectedRow);
                    int customerId = (int) rowData[0];  // Ambil ID pelanggan
                    new EditCustomerDialog((JFrame) SwingUtilities.getWindowAncestor(DataPelangganPanel.this),
                            customerId, rowData, DataPelangganPanel.this).setVisible(true);
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void fetchAndDisplayData() {
        allData = new ArrayList<>();
        try {
            ResultSet rs = dbManager.fetchPelangganData();
            while (rs.next()) {
                allData.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nama_pelanggan"),
                        rs.getString("nomor_telepon"),
                        rs.getString("alamat"),
                        rs.getString("email"),
                        rs.getTimestamp("created_at")
                });
            }

            updateTableData();
            System.out.println("Data pelanggan berhasil dimuat, jumlah data: " + allData.size());

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mengambil data pelanggan dari database.", "Error", JOptionPane.ERROR_MESSAGE);
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

    // Tambahkan metode refreshData untuk memperbarui tabel setelah edit
    public void refreshData() {
        fetchAndDisplayData();
    }
}
