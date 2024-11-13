package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DataTablePanel extends JPanel {

    private static final int ROWS_PER_PAGE = 56;
    private int currentPage = 1;
    private List<Object[]> allData;
    private DefaultTableModel model;
    private JTable table;
    private DatabaseManager dbManager;

    public DataTablePanel() {
        setLayout(new BorderLayout());
        dbManager = DatabaseManager.getInstance();

        // Ubah header tabel sesuai dengan nama yang akan ditampilkan
        model = new DefaultTableModel(new String[]{
                "ID", "Nama Pelanggan", "Nama Mobil", "Nama Sopir",
                "Tanggal Mulai", "Tanggal Selesai", "Tanggal Kembali",
                "Total Harga", "Status", "Denda", "Created At"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Semua sel tidak bisa diedit
            }
        };

        // Inisialisasi tabel
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Tambahkan MouseListener untuk membuka EditDialog saat baris diklik dua kali
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {  // Klik dua kali
                    int selectedRow = table.convertRowIndexToModel(table.getSelectedRow());
                    openEditDialog(selectedRow);
                }
            }
        });

        // Ambil dan tampilkan data dari database
        fetchAndDisplayData();

        // Tambahkan panel untuk pagination
        JPanel paginationPanel = createPaginationPanel();
        add(paginationPanel, BorderLayout.SOUTH);
    }

    // Fungsi untuk membuka EditDialog
    private void openEditDialog(int rowIndex) {
        Object[] rowData = allData.get(rowIndex);
        EditDialog editDialog = new EditDialog(SwingUtilities.getWindowAncestor(this), rowData, dbManager, rowIndex, this, true);
        editDialog.setVisible(true);
    }

    public void fetchAndDisplayData() {
        allData = new ArrayList<>();
        try {
            // Query dengan JOIN untuk mengambil nama pelanggan, mobil, dan sopir
            String query = "SELECT p.id, pel.nama_pelanggan, m.nama_mobil, s.nama_sopir, " +
                    "p.tanggal_mulai, p.tanggal_selesai, p.tanggal_kembali, " +
                    "p.total_harga, p.status_pemesanan, p.denda, p.created_at " +
                    "FROM pemesan_mobil p " +
                    "JOIN pelanggan pel ON p.id_pelanggan = pel.id " +
                    "JOIN mobil m ON p.id_mobil = m.id " +
                    "JOIN sopir s ON p.id_sopir = s.id " +
                    "ORDER BY p.id DESC";

            ResultSet rs = dbManager.executeQuery(query);
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", new Locale("id", "ID"));
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

            while (rs.next()) {
                String tanggalMulai = rs.getTimestamp("tanggal_mulai") != null ? dateFormatter.format(rs.getTimestamp("tanggal_mulai")) : "";
                String tanggalSelesai = rs.getTimestamp("tanggal_selesai") != null ? dateFormatter.format(rs.getTimestamp("tanggal_selesai")) : "";
                String tanggalKembali = rs.getDate("tanggal_kembali") != null ? dateFormatter.format(rs.getDate("tanggal_kembali")) : "";

                allData.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nama_pelanggan"),
                        rs.getString("nama_mobil"),
                        rs.getString("nama_sopir"),
                        tanggalMulai,
                        tanggalSelesai,
                        tanggalKembali,
                        currencyFormatter.format(rs.getDouble("total_harga")),
                        rs.getString("status_pemesanan"),
                        currencyFormatter.format(rs.getDouble("denda")),
                        rs.getTimestamp("created_at") != null ? dateFormatter.format(rs.getTimestamp("created_at")) : ""
                });
            }

            displayPage(1);
            System.out.println("Data berhasil dimuat, jumlah data: " + allData.size());

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mengambil data dari database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayPage(int pageNumber) {
        model.setRowCount(0); // Hapus data sebelumnya dari model tabel
        int start = (pageNumber - 1) * ROWS_PER_PAGE;
        int end = Math.min(start + ROWS_PER_PAGE, allData.size());

        for (int i = start; i < end; i++) {
            model.addRow(allData.get(i));
        }

        currentPage = pageNumber;
    }

    private JPanel createPaginationPanel() {
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnPrevious = new JButton("Previous");
        JButton btnNext = new JButton("Next");

        btnPrevious.addActionListener(e -> {
            if (currentPage > 1) {
                displayPage(currentPage - 1);
            }
        });

        btnNext.addActionListener(e -> {
            if (currentPage * ROWS_PER_PAGE < allData.size()) {
                displayPage(currentPage + 1);
            }
        });

        paginationPanel.add(btnPrevious);
        paginationPanel.add(btnNext);

        return paginationPanel;
    }

    public boolean hasData() {
        return allData != null && !allData.isEmpty();
    }
}
