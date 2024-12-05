package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataSopirPanel extends JPanel {


    private static final int ROWS_PER_PAGE = 1; // Jumlah baris per halaman
    private int currentPage = 1;
    private List<Object[]> allData;
    private DefaultTableModel model;
    private JTable table;
    private DatabaseManager dbManager;

    public DataSopirPanel() {
        setLayout(new BorderLayout());
        dbManager = DatabaseManager.getInstance();

        // Setup tabel
        model = new DefaultTableModel(new String[]{
                "ID", "Nama Sopir", "Email", "Nomor Telepon", "Alamat", "Status Sopir",
                "Harga Sewa per Hari", "Created At", "Aksi"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8; // Hanya kolom "Aksi" yang dapat diinteraksi
            }
        };

        table = new JTable(model);

        // Tambahkan renderer custom untuk kolom "Aksi"
        table.getColumn("Aksi").setCellRenderer(new ButtonRenderer());
        table.getColumn("Aksi").setCellEditor(new ButtonEditor(new JCheckBox()));

        fetchAndDisplayData();

        // Tambahkan panel pagination
        JPanel paginationPanel = createPaginationPanel();
        add(paginationPanel, BorderLayout.SOUTH);

        // Tambahkan MouseListener untuk klik dua kali pada baris
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) { // Double-click
                    int selectedRow = table.convertRowIndexToModel(table.getSelectedRow());
                    Object[] rowData = allData.get(selectedRow);
                    int sopirId = (int) rowData[0]; // Ambil ID sopir
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
                        rs.getTimestamp("created_at"),
                        "Hapus" // Label tombol untuk kolom "Aksi"
                });
            }

            displayPage(1);
            System.out.println("Data sopir berhasil dimuat, jumlah data: " + allData.size());

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mengambil data sopir dari database.", "Error", JOptionPane.ERROR_MESSAGE);
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

    // Method to refresh data after editing
    public void refreshData() {
        fetchAndDisplayData();
    }

    // Renderer untuk menampilkan tombol pada kolom "Aksi"
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

    // Editor untuk menangani klik tombol pada kolom "Aksi"
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
                int sopirId = (int) allData.get(modelRow)[0];
                int confirmation = JOptionPane.showConfirmDialog(DataSopirPanel.this,
                        "Apakah Anda yakin ingin menghapus sopir ini?",
                        "Konfirmasi Hapus",
                        JOptionPane.YES_NO_OPTION);

                if (confirmation == JOptionPane.YES_OPTION) {
                    try {
                        dbManager.deleteSopirById(sopirId); // Asumsikan ada metode deleteSopirById di DatabaseManager
                        allData.remove(modelRow);
                        refreshData();
                        JOptionPane.showMessageDialog(DataSopirPanel.this, "Sopir berhasil dihapus.");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(DataSopirPanel.this, "Gagal menghapus sopir.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            clicked = false;
            return label;
        }
    }
}
