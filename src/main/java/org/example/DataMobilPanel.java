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

public class DataMobilPanel extends JPanel {

    private static final int ROWS_PER_PAGE = 1;
    private int currentPage = 1;
    private List<Object[]> allData;
    private DefaultTableModel model;
    private JTable table;
    private DatabaseManager dbManager;

    public DataMobilPanel() {
        setLayout(new BorderLayout());
        dbManager = DatabaseManager.getInstance();

        // Inisialisasi model tabel
        model = new DefaultTableModel(new String[]{
                "ID", "Foto Mobil", "Nama Mobil", "Tipe Mobil", "Tahun Mobil", "Plat Nomor",
                "Harga Sewa", "Status", "Created At", "Aksi"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 9; // Hanya kolom "Aksi" yang bisa diedit
            }
        };

        // Inisialisasi tabel
        table = new JTable(model);

        // Tambahkan renderer dan editor untuk kolom "Aksi"
        table.getColumn("Aksi").setCellRenderer(new ButtonRenderer());
        table.getColumn("Aksi").setCellEditor(new ButtonEditor(new JCheckBox()));

        fetchAndDisplayData();

        // Tambahkan pagination panel
        JPanel paginationPanel = createPaginationPanel();
        add(paginationPanel, BorderLayout.SOUTH);

        // Tambahkan mouse listener untuk klik dua kali
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    int selectedRow = table.convertRowIndexToModel(table.getSelectedRow());
                    Object[] rowData = allData.get(selectedRow);
                    int carId = (int) rowData[0];
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
            ResultSet rs = dbManager.fetchMobilData(); // Metode fetchMobilData di DatabaseManager
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
                        rs.getTimestamp("created_at"),
                        "Hapus"
                });
            }

            displayPage(1);
            System.out.println("Data mobil berhasil dimuat, jumlah data: " + allData.size());

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mengambil data mobil dari database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayPage(int pageNumber) {
        model.setRowCount(0);
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

    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(Color.RED);
            setForeground(Color.WHITE);
            setFocusable(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean clicked;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(Color.RED);
            button.setForeground(Color.WHITE);
            button.setFocusable(false);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = value == null ? "" : value.toString();
            button.setText(label);
            clicked = true;
            selectedRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                int modelRow = table.convertRowIndexToModel(selectedRow);
                int dataId = (int) allData.get(modelRow)[0];
                int confirmation = JOptionPane.showConfirmDialog(DataMobilPanel.this,
                        "Apakah Anda yakin ingin menghapus mobil ini?",
                        "Konfirmasi Hapus",
                        JOptionPane.YES_NO_OPTION);

                if (confirmation == JOptionPane.YES_OPTION) {
                    try {
                        dbManager.deleteMobilById(dataId);  // Implementasi hapus data di DatabaseManager
                        allData.remove(modelRow);
                        refreshData();
                        JOptionPane.showMessageDialog(DataMobilPanel.this, "Mobil berhasil dihapus.");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(DataMobilPanel.this, "Gagal menghapus mobil.", "Error", JOptionPane.ERROR_MESSAGE);
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