package org.example;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditDialog extends JDialog {

    private JComboBox<String> idMobilDropdown;
    private JComboBox<String> idPelangganDropdown;
    private JComboBox<String> idSopirDropdown;
    private JTextField tanggalMulaiField;
    private JTextField tanggalSelesaiField;
    private JTextField tanggalKembaliField;
    private JFormattedTextField totalHargaField;
    private JComboBox<String> statusField;
    private JFormattedTextField dendaField;
    private DatabaseManager dbManager;
    private int rowIndex;
    private Object[] rowData;
    private DataTablePanel dataTablePanel;
    private boolean isEditMode;
    private Map<String, Integer> mobilMap, pelangganMap, sopirMap;
    private double mobilHargaPerHari, sopirHargaPerHari;

    public EditDialog(Window parent, Object[] rowData, DatabaseManager dbManager, int rowIndex, DataTablePanel dataTablePanel, boolean isEditMode) {
        super(parent, isEditMode ? "Edit Data" : "Tambah Data", ModalityType.APPLICATION_MODAL);
        this.dbManager = dbManager;
        this.rowData = rowData;
        this.rowIndex = rowIndex;
        this.dataTablePanel = dataTablePanel;
        this.isEditMode = isEditMode;

        setSize(600, 500);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);

        String pelanggan = rowData[2] != null ? rowData[2].toString() : "";
        String mobil = rowData[1] != null ? rowData[1].toString() : "";
        String sopir = rowData[3] != null ? rowData[3].toString() : "";

        idPelangganDropdown = createDropdown("pelanggan", "nama_pelanggan", false, "Data Pelanggan", pelanggan);
        idMobilDropdown = createDropdown("mobil", "nama_mobil", true, "Data Mobil", mobil);
        idSopirDropdown = createDropdown("sopir", "nama_sopir", true, "Data Sopir", sopir);

        totalHargaField = createCurrencyField(rowData[7].toString());
        statusField = new JComboBox<>(new String[]{"pending", "disetujui", "dibatalkan", "selesai", "terlambat"});
        statusField.setSelectedItem(rowData[8].toString());
        dendaField = createCurrencyField(rowData[9].toString());

        // Set default dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String todayDate = dateFormat.format(new Date());

        // Initialize tanggalMulaiField and tanggalSelesaiField with default today date
        tanggalMulaiField = new JTextField(isEditMode ? rowData[4].toString() : todayDate, 15);
        JButton tanggalMulaiButton = createDateButton(tanggalMulaiField);

        // Set tanggalSelesaiField to today date or existing value if in edit mode
        tanggalSelesaiField = new JTextField(isEditMode ? rowData[5].toString() : todayDate, 15);
        JButton tanggalSelesaiButton = createDateButton(tanggalSelesaiField);

        tanggalKembaliField = new JTextField(rowData[6] != null ? rowData[6].toString() : "", 15);
        JButton tanggalKembaliButton = createDateButton(tanggalKembaliField);

        addLabelAndField("Data Pelanggan:", idPelangganDropdown, gbc, 0);
        addLabelAndField("Data Mobil:", idMobilDropdown, gbc, 1);
        addLabelAndField("Data Sopir:", idSopirDropdown, gbc, 2);
        addLabelAndFieldWithButton("Tanggal Mulai:", tanggalMulaiField, tanggalMulaiButton, gbc, 3);
        addLabelAndFieldWithButton("Tanggal Selesai:", tanggalSelesaiField, tanggalSelesaiButton, gbc, 4);
        addLabelAndFieldWithButton("Tanggal Kembali:", tanggalKembaliField, tanggalKembaliButton, gbc, 5);
        addLabelAndField("Total Harga:", totalHargaField, gbc, 6);
        addLabelAndComboBox("Status:", statusField, gbc, 7);
        addLabelAndField("Denda:", dendaField, gbc, 8);

        JButton saveButton = new JButton("Simpan");
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(saveButton, gbc);

        saveButton.addActionListener(e -> saveToDatabase());

        tanggalMulaiField.addActionListener(e -> updateTotalHarga());
        tanggalSelesaiField.addActionListener(e -> updateTotalHarga());
        idMobilDropdown.addActionListener(e -> fetchMobilHargaPerHari());
        idSopirDropdown.addActionListener(e -> fetchSopirHargaPerHari());
    }

    private JComboBox<String> createDropdown(String tableName, String columnName, boolean fetchPrice, String defaultOption, String selectedItem) {
        JComboBox<String> comboBox = new JComboBox<>();
        Map<String, Integer> map = new HashMap<>();
        if (!isEditMode) {
            comboBox.addItem(defaultOption);
        }

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, " + columnName + (fetchPrice ? ", harga_sewa_per_hari" : "") + " FROM " + tableName)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString(columnName);
                map.put(name, id);
                comboBox.addItem(name);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data from " + tableName, "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        if (tableName.equals("mobil")) mobilMap = map;
        else if (tableName.equals("pelanggan")) pelangganMap = map;
        else if (tableName.equals("sopir")) sopirMap = map;

        if (isEditMode && selectedItem != null) {
            comboBox.setSelectedItem(selectedItem);
        } else if (!isEditMode) {
            comboBox.setSelectedIndex(0);
        }

        return comboBox;
    }

    private void fetchMobilHargaPerHari() {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT harga_sewa_per_hari FROM mobil WHERE id = ?")) {
            stmt.setInt(1, mobilMap.get(idMobilDropdown.getSelectedItem().toString()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                mobilHargaPerHari = rs.getDouble("harga_sewa_per_hari");
                updateTotalHarga();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading harga from Mobil", "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void fetchSopirHargaPerHari() {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT harga_sewa_per_hari FROM sopir WHERE id = ?")) {
            stmt.setInt(1, sopirMap.get(idSopirDropdown.getSelectedItem().toString()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                sopirHargaPerHari = rs.getDouble("harga_sewa_per_hari");
                updateTotalHarga();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading harga from Sopir", "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private JButton createDateButton(JTextField dateField) {
        JButton button = new JButton("Pilih Tanggal");
        button.addActionListener(e -> {
            Date selectedDate = showDatePicker();
            if (selectedDate != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                dateField.setText(dateFormat.format(selectedDate));

                if (dateField == tanggalMulaiField) {
                    Date endDate = new Date(selectedDate.getTime() + 24 * 60 * 60 * 1000); // Add 1 day
                    tanggalSelesaiField.setText(dateFormat.format(endDate));
                }

                updateTotalHarga();
            }
        });
        return button;
    }

    private Date showDatePicker() {
        JDateChooser dateChooser = new JDateChooser();
        int result = JOptionPane.showConfirmDialog(this, dateChooser, "Pilih Tanggal", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) return dateChooser.getDate();
        return null;
    }

    private void updateTotalHarga() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date startDate = dateFormat.parse(tanggalMulaiField.getText());
            Date endDate = dateFormat.parse(tanggalSelesaiField.getText());

            long daysBetween = ChronoUnit.DAYS.between(startDate.toInstant(), endDate.toInstant());

            double totalHarga = (mobilHargaPerHari + sopirHargaPerHari) * (daysBetween + 1);
            totalHargaField.setValue(totalHarga);
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveToDatabase() {

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    performSave();
                } catch (SQLException | ParseException e) {
                    JOptionPane.showMessageDialog(EditDialog.this, "Error saving data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions during save
                    dataTablePanel.fetchAndDisplayData(); // Refresh table data
                    dispose(); // Close the EditDialog after saving
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    private void performSave() throws SQLException, ParseException {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Mengambil nilai tanggal dari field dan memeriksa apakah tidak kosong
        String tanggalMulaiText = tanggalMulaiField.getText().trim();
        String tanggalSelesaiText = tanggalSelesaiField.getText().trim();
        String tanggalKembaliText = tanggalKembaliField.getText().trim();

        // Validasi dan parsing tanggal hanya jika tidak kosong
        String formattedTanggalMulai = null;
        String formattedTanggalSelesai = null;
        String formattedTanggalKembali = null;

        if (!tanggalMulaiText.isEmpty()) {
            formattedTanggalMulai = dbDateFormat.format(inputDateFormat.parse(tanggalMulaiText));
        } else {
            JOptionPane.showMessageDialog(this, "Tanggal Mulai harus diisi.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!tanggalSelesaiText.isEmpty()) {
            formattedTanggalSelesai = dbDateFormat.format(inputDateFormat.parse(tanggalSelesaiText));
        } else {
            JOptionPane.showMessageDialog(this, "Tanggal Selesai harus diisi.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!tanggalKembaliText.isEmpty()) {
            formattedTanggalKembali = dbDateFormat.format(inputDateFormat.parse(tanggalKembaliText));
        }

        // Menghitung denda jika tanggal kembali melewati hari ini
        double denda = 0.0;
        if (!tanggalKembaliText.isEmpty()) {
            Date today = new Date();
            Date tanggalKembali = inputDateFormat.parse(tanggalKembaliText);

            if (tanggalKembali.before(today)) {
                long daysLate = ChronoUnit.DAYS.between(tanggalKembali.toInstant(), today.toInstant());
                denda = daysLate * (mobilHargaPerHari + sopirHargaPerHari); // Menghitung denda
                dendaField.setValue(denda); // Menampilkan denda pada field denda
            }
        }

        try (Connection conn = dbManager.getConnection()) {
            String query;
            if (isEditMode) {
                query = "UPDATE pemesan_mobil SET id_mobil = ?, id_pelanggan = ?, id_sopir = ?, tanggal_mulai = ?, tanggal_selesai = ?, tanggal_kembali = ?, total_harga = ?, status_pemesanan = ?, denda = ? WHERE id = ?";
            } else {
                query = "INSERT INTO pemesan_mobil (id_mobil, id_pelanggan, id_sopir, tanggal_mulai, tanggal_selesai, tanggal_kembali, total_harga, status_pemesanan, denda) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            }

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, mobilMap.get(idMobilDropdown.getSelectedItem().toString()));
            pstmt.setInt(2, pelangganMap.get(idPelangganDropdown.getSelectedItem().toString()));
            pstmt.setInt(3, sopirMap.get(idSopirDropdown.getSelectedItem().toString()));
            pstmt.setString(4, formattedTanggalMulai);
            pstmt.setString(5, formattedTanggalSelesai);

            // Mengatur tanggal kembali sebagai NULL jika kosong
            if (formattedTanggalKembali != null) {
                pstmt.setString(6, formattedTanggalKembali);
            } else {
                pstmt.setNull(6, java.sql.Types.DATE);
            }

            pstmt.setDouble(7, Double.parseDouble(totalHargaField.getValue().toString().replace(",", ".")));
            pstmt.setString(8, statusField.getSelectedItem().toString());
            pstmt.setDouble(9, denda);

            if (isEditMode) {
                pstmt.setInt(10, Integer.parseInt(rowData[0].toString()));
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Data berhasil " + (isEditMode ? "diperbarui." : "disimpan."), "Sukses", JOptionPane.INFORMATION_MESSAGE);
                dataTablePanel.fetchAndDisplayData();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal " + (isEditMode ? "memperbarui" : "menyimpan") + " data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private JDialog createProgressDialog() {
        JDialog dialog = new JDialog(this, "Loading", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(this);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        dialog.add(progressBar, BorderLayout.CENTER);

        JLabel label = new JLabel("Mohon tunggu sebentar, data sedang disimpan...", JLabel.CENTER);
        dialog.add(label, BorderLayout.NORTH);

        return dialog;
    }


    // Other existing methods (createCurrencyField, addLabelAndField, etc.)

    private JFormattedTextField createCurrencyField(String valueText) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        NumberFormatter currencyFormatter = new NumberFormatter(currencyFormat);
        currencyFormatter.setValueClass(Double.class);
        currencyFormatter.setMinimum(0.0);
        currencyFormatter.setAllowsInvalid(false);

        JFormattedTextField currencyField = new JFormattedTextField(currencyFormatter);
        currencyField.setColumns(15);

        if (valueText == null || valueText.isEmpty()) {
            currencyField.setValue(0.0);
        } else {
            currencyField.setValue(Double.parseDouble(valueText.replaceAll("[^\\d,]", "").replace(",", ".")));
        }

        return currencyField;
    }

    private void addLabelAndField(String labelText, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        add(field, gbc);
    }

    private void addLabelAndFieldWithButton(String labelText, JTextField textField, JButton button, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        add(textField, gbc);

        gbc.gridx = 2;
        add(button, gbc);
    }

    private void addLabelAndComboBox(String labelText, JComboBox<String> comboBox, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        add(comboBox, gbc);
    }
}
