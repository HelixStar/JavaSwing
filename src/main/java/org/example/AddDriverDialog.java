package org.example;

import javax.swing.*;
import java.awt.*;

public class AddDriverDialog extends JDialog {
    private DataSopirPanel sopirPanel;

    public AddDriverDialog(JFrame parent, DataSopirPanel sopirPanel) {
        super(parent, "Tambah Sopir", true);
        this.sopirPanel = sopirPanel; // Save reference to sopir panel
        setSize(400, 400);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Adding input fields
        JTextField namaField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField nomorTeleponField = new JTextField(20);
        JTextField alamatField = new JTextField(20);
        JComboBox<String> statusSopirField = new JComboBox<>(new String[]{"tersedia", "disewa"});
        JTextField hargaSewaField = new JTextField(20);

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
        add(new JLabel("Password:"), gbc);

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
        saveButton.addActionListener(e -> saveDriver(namaField.getText(), emailField.getText(), new String(passwordField.getPassword()),
                nomorTeleponField.getText(), alamatField.getText(), (String) statusSopirField.getSelectedItem(),
                hargaSewaField.getText()));

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(saveButton, gbc);
    }

    private void saveDriver(String nama, String email, String password, String nomorTelepon, String alamat, String statusSopir, String hargaSewa) {
        // Input validation
        if (nama.isEmpty() || email.isEmpty() || password.isEmpty() || nomorTelepon.isEmpty() || alamat.isEmpty() || statusSopir.isEmpty() || hargaSewa.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Save to database
        DatabaseManager dbManager = DatabaseManager.getInstance();
        String query = "INSERT INTO sopir (nama_sopir, email, password, nomor_telepon, alamat, status_sopir, harga_sewa_per_hari, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        Object[] params = {nama, email, password, nomorTelepon, alamat, statusSopir, new java.math.BigDecimal(hargaSewa)};
        int rowsInserted = dbManager.updateData(query, params);

        if (rowsInserted > 0) {
            JOptionPane.showMessageDialog(this, "Sopir berhasil ditambahkan.");
            dispose(); // Close dialog after successful save
            sopirPanel.refreshData(); // Refresh driver data on main panel
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menambahkan sopir.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
