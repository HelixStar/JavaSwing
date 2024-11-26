package org.example;

import javax.swing.*;
import java.awt.*;

public class AddCustomerDialog extends JDialog {


    private DataPelangganPanel pelangganPanel;

    public AddCustomerDialog(JFrame parent, DataPelangganPanel pelangganPanel) {
        super(parent, "Tambah Pelanggan", true);
        this.pelangganPanel = pelangganPanel; // Simpan referensi ke panel pelanggan
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Menambahkan field input
        JTextField namaField = new JTextField(20);
        JTextField nomorTeleponField = new JTextField(20);
        JTextField alamatField = new JTextField(20);
        JTextField emailField = new JTextField(20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Nama Pelanggan:"), gbc);

        gbc.gridx = 1;
        add(namaField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Nomor Telepon:"), gbc);

        gbc.gridx = 1;
        add(nomorTeleponField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Alamat:"), gbc);

        gbc.gridx = 1;
        add(alamatField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        add(emailField, gbc);

        // Tombol simpan
        JButton saveButton = new JButton("Simpan");
        saveButton.addActionListener(e -> saveCustomer(namaField.getText(), nomorTeleponField.getText(), alamatField.getText(), emailField.getText()));

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(saveButton, gbc);
    }

    private void saveCustomer(String nama, String nomorTelepon, String alamat, String email) {
        // Validasi input
        if (nama.isEmpty() || nomorTelepon.isEmpty() || alamat.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validasi format email
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            JOptionPane.showMessageDialog(this, "Email tidak valid. Harap masukkan email dengan format yang benar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validasi nomor telepon
        if (!nomorTelepon.matches("\\d{11,}")) {
            JOptionPane.showMessageDialog(this, "Nomor telepon harus berupa angka dan minimal 11 digit.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Simpan ke database
        DatabaseManager dbManager = DatabaseManager.getInstance();
        String query = "INSERT INTO pelanggan (nama_pelanggan, nomor_telepon, alamat, email, created_at) VALUES (?, ?, ?, ?, NOW())";
        Object[] params = {nama, nomorTelepon, alamat, email};
        int rowsInserted = dbManager.updateData(query, params);

        if (rowsInserted > 0) {
            JOptionPane.showMessageDialog(this, "Pelanggan berhasil ditambahkan.");
            dispose(); // Tutup dialog setelah berhasil simpan
            pelangganPanel.refreshData(); // Refresh data pelanggan di panel utama
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menambahkan pelanggan.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
