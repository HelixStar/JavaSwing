package org.example;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class EditCustomerDialog extends JDialog {

    private JTextField namaField;
    private JTextField nomorTeleponField;
    private JTextField alamatField;
    private JTextField emailField;
    private DatabaseManager dbManager;
    private int customerId;
    private DataPelangganPanel pelangganPanel;

    public EditCustomerDialog(JFrame parent, int customerId, Object[] customerData, DataPelangganPanel pelangganPanel) {
        super(parent, "Edit Data Pelanggan", true);
        this.dbManager = DatabaseManager.getInstance();
        this.customerId = customerId;
        this.pelangganPanel = pelangganPanel;

        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Menambahkan field input
        namaField = new JTextField(customerData[1].toString(), 20);
        nomorTeleponField = new JTextField(customerData[2].toString(), 20);
        alamatField = new JTextField(customerData[3].toString(), 20);
        emailField = new JTextField(customerData[4].toString(), 20);

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
        saveButton.addActionListener(e -> saveCustomerData());

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(saveButton, gbc);
    }

    private void saveCustomerData() {
        String nama = namaField.getText();
        String nomorTelepon = nomorTeleponField.getText();
        String alamat = alamatField.getText();
        String email = emailField.getText();

        // Update data di database
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

        // Update data di database
        String query = "UPDATE pelanggan SET nama_pelanggan = ?, nomor_telepon = ?, alamat = ?, email = ? WHERE id = ?";
        Object[] params = {nama, nomorTelepon, alamat, email, customerId};

        int rowsUpdated = dbManager.updateData(query, params);

        if (rowsUpdated > 0) {
            JOptionPane.showMessageDialog(this, "Data pelanggan berhasil diperbarui.");
            pelangganPanel.refreshData(); // Refresh data di tabel setelah update
            dispose(); // Tutup dialog
        } else {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui data pelanggan.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
