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

public class DataMobilPanel extends JPanel{
    private List<Object[]> allData;
    private DefaultTableModel model;
    private JTable table;
    private DatabaseManager dbManager;

    public DataMobilPanel() {
        setLayout(new BorderLayout());
        dbManager = DatabaseManager.getInstance();
        model = new DefaultTableModel(new String[]{
                "ID", "Foto Mobil", "Nama Mobil", "Tipe Mobil", "Tahun Mobil", "Plat Nomor", "Harga Sewa", "Status", "Created At"
        }, 0) {
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
                    int carId = (int) rowData[0];  // Ambil ID mobil
                    new EditCarDialog((JFrame) SwingUtilities.getWindowAncestor(DataMobilPanel.this),
                            carId, rowData, DataMobilPanel.this).setVisible(true);
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void fetchAndDisplayData() {
        allData = new ArrayList<>();
        try {
            ResultSet rs = dbManager.fetchMobilData(); // Asumsikan ada metode fetchMobilData() di DatabaseManager
            while (rs.next()) {
                allData.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("foto_mobil"),
                        rs.getString("nama_mobil"),
                        rs.getString("tipe_mobil"),
                        rs.getInt("tahun_mobil"),
                        rs.getString("plat_nomor"),
                        rs.getBigDecimal("harga_sewa_per_hari"),
                        rs.getString("status_mobil"),
                        rs.getTimestamp("created_at")
                });
            }

            updateTableData();
            System.out.println("Data mobil berhasil dimuat, jumlah data: " + allData.size());

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mengambil data mobil dari database.", "Error", JOptionPane.ERROR_MESSAGE);
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
