package org.example;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class EditCarDialog extends JDialog{
    private JTextField namaMobilField;
    private JTextField tipeMobilField;
    private JTextField tahunMobilField;
    private JTextField platNomorField;
    private JTextField hargaSewaField;
    private JComboBox<String> statusMobilCombo;
    private DatabaseManager dbManager;
    private int carId;
    private DataMobilPanel mobilPanel;

    public EditCarDialog(JFrame parent, int carId, Object[] carData, DataMobilPanel mobilPanel) {
        super(parent, "Edit Data Mobil", true);
        this.dbManager = DatabaseManager.getInstance();
        this.carId = carId;
        this.mobilPanel = mobilPanel;

        setSize(400, 400);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Menambahkan field input
        namaMobilField = new JTextField(carData[2].toString(), 20);
        tipeMobilField = new JTextField(carData[3].toString(), 20);
        tahunMobilField = new JTextField(carData[4].toString(), 20);
        platNomorField = new JTextField(carData[5].toString(), 20);
        hargaSewaField = new JTextField(carData[6].toString(), 20);
        statusMobilCombo = new JComboBox<>(new String[]{"tersedia", "tidak tersedia"});
        statusMobilCombo.setSelectedItem(carData[7].toString());

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Nama Mobil:"), gbc);

        gbc.gridx = 1;
        add(namaMobilField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Tipe Mobil:"), gbc);

        gbc.gridx = 1;
        add(tipeMobilField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Tahun Mobil:"), gbc);

        gbc.gridx = 1;
        add(tahunMobilField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Plat Nomor:"), gbc);

        gbc.gridx = 1;
        add(platNomorField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        add(new JLabel("Harga Sewa per Hari:"), gbc);

        gbc.gridx = 1;
        add(hargaSewaField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        add(new JLabel("Status Mobil:"), gbc);

        gbc.gridx = 1;
        add(statusMobilCombo, gbc);

        // Tombol simpan
        JButton saveButton = new JButton("Simpan");
        saveButton.addActionListener(e -> saveCarData());

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(saveButton, gbc);
    }

    private void saveCarData() {
        String namaMobil = namaMobilField.getText();
        String tipeMobil = tipeMobilField.getText();
        String tahunMobil = tahunMobilField.getText();
        String platNomor = platNomorField.getText();
        String hargaSewa = hargaSewaField.getText();
        String statusMobil = (String) statusMobilCombo.getSelectedItem();

        // Update data di database
        String query = "UPDATE mobil SET nama_mobil = ?, tipe_mobil = ?, tahun_mobil = ?, plat_nomor = ?, harga_sewa_per_hari = ?, status_mobil = ? WHERE id = ?";
        Object[] params = {namaMobil, tipeMobil, Integer.parseInt(tahunMobil), platNomor, new java.math.BigDecimal(hargaSewa), statusMobil, carId};

        int rowsUpdated = dbManager.updateData(query, params);

        if (rowsUpdated > 0) {
            JOptionPane.showMessageDialog(this, "Data mobil berhasil diperbarui.");
            mobilPanel.refreshData(); // Refresh data di tabel setelah update
            dispose(); // Tutup dialog
        } else {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui data mobil.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
