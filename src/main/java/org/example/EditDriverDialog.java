package org.example;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class EditDriverDialog extends JDialog {
    private JTextField namaField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField nomorTeleponField;
    private JTextField alamatField;
    private JComboBox<String> statusSopirField;
    private JTextField hargaSewaField;
    private DatabaseManager dbManager;
    private int sopirId;
    private DataSopirPanel sopirPanel;

    public EditDriverDialog(JFrame parent, int sopirId, Object[] sopirData, DataSopirPanel sopirPanel) {
        super(parent, "Edit Data Sopir", true);
        this.dbManager = DatabaseManager.getInstance();
        this.sopirId = sopirId;
        this.sopirPanel = sopirPanel;

        setSize(400, 400);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add input fields
        namaField = new JTextField(sopirData[1].toString(), 20);
        emailField = new JTextField(sopirData[2].toString(), 20);
        passwordField = new JPasswordField(20);  // New password entry, leave empty if unchanged
        nomorTeleponField = new JTextField(sopirData[3].toString(), 20);
        alamatField = new JTextField(sopirData[4].toString(), 20);
        statusSopirField = new JComboBox<>(new String[]{"tersedia", "disewa"});
        statusSopirField.setSelectedItem(sopirData[5].toString());
        hargaSewaField = new JTextField(sopirData[6].toString(), 20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Nama Sopir:"), gbc);
        gbc.gridx = 1;
        add(namaField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Password (leave empty if unchanged):"), gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Nomor Telepon:"), gbc);
        gbc.gridx = 1;
        add(nomorTeleponField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        add(new JLabel("Alamat:"), gbc);
        gbc.gridx = 1;
        add(alamatField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        add(new JLabel("Status Sopir:"), gbc);
        gbc.gridx = 1;
        add(statusSopirField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        add(new JLabel("Harga Sewa per Hari:"), gbc);
        gbc.gridx = 1;
        add(hargaSewaField, gbc);

        // Save button
        JButton saveButton = new JButton("Simpan");
        saveButton.addActionListener(e -> saveDriverData());

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(saveButton, gbc);
    }

    private void saveDriverData() {
        String nama = namaField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword()).trim();
        String nomorTelepon = nomorTeleponField.getText();
        String alamat = alamatField.getText();
        String statusSopir = (String) statusSopirField.getSelectedItem();
        String hargaSewa = hargaSewaField.getText();

        // Construct query and parameters based on whether password is provided
        String query;
        Object[] params;

        if (password.isEmpty()) {
            query = "UPDATE sopir SET nama_sopir = ?, email = ?, nomor_telepon = ?, alamat = ?, status_sopir = ?, harga_sewa_per_hari = ? WHERE id = ?";
            params = new Object[]{nama, email, nomorTelepon, alamat, statusSopir, new java.math.BigDecimal(hargaSewa), sopirId};
        } else {
            query = "UPDATE sopir SET nama_sopir = ?, email = ?, password = ?, nomor_telepon = ?, alamat = ?, status_sopir = ?, harga_sewa_per_hari = ? WHERE id = ?";
            params = new Object[]{nama, email, password, nomorTelepon, alamat, statusSopir, new java.math.BigDecimal(hargaSewa), sopirId};
        }

        int rowsUpdated = dbManager.updateData(query, params);

        if (rowsUpdated > 0) {
            JOptionPane.showMessageDialog(this, "Data sopir berhasil diperbarui.");
            sopirPanel.refreshData(); // Refresh data in table after update
            dispose(); // Close dialog
        } else {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui data sopir.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
