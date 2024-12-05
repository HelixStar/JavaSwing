package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
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

    private static final int ROWS_PER_PAGE = 1;
    private int currentPage = 1;
    private List<Object[]> allData;
    private DefaultTableModel model;
    private JTable table;
    private DatabaseManager dbManager;

    public DataTablePanel() {
        setLayout(new BorderLayout());
        dbManager = DatabaseManager.getInstance();

        // Setup model tabel
        model = new DefaultTableModel(new String[]{
                "ID", "Nama Pelanggan", "Nama Mobil", "Nama Sopir",
                "Tanggal Mulai", "Tanggal Selesai", "Tanggal Kembali",
                "Total Harga", "Status", "Denda", "Created At", "Aksi"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 11; // Hanya kolom "Aksi" yang dapat diinteraksi
            }
        };

        // Inisialisasi tabel
        table = new JTable(model);

        // Tambahkan renderer untuk tombol pada kolom "Aksi"
        table.getColumn("Aksi").setCellRenderer(new ButtonRenderer());
        table.getColumn("Aksi").setCellEditor(new ButtonEditor(new JCheckBox()));

        // Tambahkan MouseListener untuk klik dua kali pada baris tabel
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {  // Klik dua kali
                    int selectedRow = table.convertRowIndexToModel(table.getSelectedRow());
                    openEditDialog(selectedRow);
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Ambil data dari database
        fetchAndDisplayData();

        // Tambahkan panel pagination
        JPanel paginationPanel = createPaginationPanel();
        add(paginationPanel, BorderLayout.SOUTH);
    }

    private void openEditDialog(int rowIndex) {
        Object[] rowData = allData.get(rowIndex);
        EditDialog editDialog = new EditDialog(SwingUtilities.getWindowAncestor(this), rowData, dbManager, rowIndex, this, true);
        editDialog.setVisible(true);
    }

    public void fetchAndDisplayData() {
        allData = new ArrayList<>();
        try {
            // Query JOIN
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
                        rs.getTimestamp("created_at") != null ? dateFormatter.format(rs.getTimestamp("created_at")) : "",
                        "Hapus"
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
        model.setRowCount(0);
        int start = (pageNumber - 1) * ROWS_PER_PAGE;
        int end = Math.min(start + ROWS_PER_PAGE, allData.size());

        // Urutkan data berdasarkan ID atau kolom lain jika perlu
        allData.sort((a, b) -> Integer.compare((Integer) a[0], (Integer) b[0]));  // Urutkan berdasarkan ID

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

    // Renderer untuk tombol
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(Color.RED);
            setForeground(Color.WHITE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    // Editor untuk menangani aksi klik pada tombol
    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean clicked;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = value == null ? "" : value.toString();
            button.setText(label);
            button.setBackground(Color.RED);
            button.setForeground(Color.WHITE);
            clicked = true;
            selectedRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                int modelRow = table.convertRowIndexToModel(selectedRow);
                int dataId = (int) allData.get(modelRow)[0];
                int confirmation = JOptionPane.showConfirmDialog(DataTablePanel.this,
                        "Apakah Anda yakin ingin menghapus data ini?",
                        "Konfirmasi Hapus",
                        JOptionPane.YES_NO_OPTION);

                if (confirmation == JOptionPane.YES_OPTION) {
                    try {
                        dbManager.deleteDataById(dataId);
                        allData.remove(modelRow);
                        refreshData();
                        JOptionPane.showMessageDialog(DataTablePanel.this, "Data berhasil dihapus.");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(DataTablePanel.this, "Gagal menghapus data.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            clicked = false;
            return label;
        }
    }

    public void refreshData() {
        fetchAndDisplayData();
    }
}