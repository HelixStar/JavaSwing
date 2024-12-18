package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

public class Main extends JFrame {

    private JPanel contentArea;
    private DataTablePanel dataTablePanel;
    private DataPelangganPanel dataPelangganPanel;
    private DataMobilPanel dataMobilPanel;
    private DataSopirPanel dataSopirPanel;

    // Variabel untuk menyimpan referensi ke menu dinamis
    private JMenu tambahPelangganMenu;
    private JMenu tambahPemesanMenu;
    private JMenu tambahMobilMenu;
    private JMenu tambahSopirMenu;
    private JMenuBar menuBar;


    public Main() {

        if (!showLoginDialog()) {
            System.exit(0);
        }

        setTitle("Aplikasi Pemesanan Mobil");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Membuat menu bar dengan styling khusus
        menuBar = new JMenuBar();
        menuBar.setBackground(new Color(0, 121, 107));
        Font menuFont = new Font("SanSerif", Font.BOLD, 14);

        // Menambahkan item menu utama
        menuBar.add(createMenu("Home", menuFont, e -> showHome()));
        menuBar.add(createMenu("Data Pelanggan", menuFont, e -> showDataPelanggan()));
        menuBar.add(createMenu("Data Mobil", menuFont, e -> showDataMobil()));
        menuBar.add(createMenu("Data Sopir", menuFont, e -> showDataSopir())); // Add "Data Sopir" menu


        // Menambahkan menu dinamis
        tambahPelangganMenu = createStyledMenu("Tambah Pelanggan", menuFont, e -> openAddCustomerDialog());
        tambahPemesanMenu = createStyledMenu("Tambah Pemesanan", menuFont, e -> openAddOrderDialog());
        tambahMobilMenu = createStyledMenu("Tambah Mobil", menuFont, e -> openAddCarDialog());
        tambahSopirMenu = createStyledMenu("Tambah Sopir", menuFont, e -> openAddDriverDialog());

        // Menu di sebelah kanan, tampilkan menu dinamis (awalnya "Tambah Pemesanan" untuk halaman Home)
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(tambahPemesanMenu); // Awalnya tampilkan "Tambah Pemesanan"

        // Set menu bar
        setJMenuBar(menuBar);

        // Area konten dengan tabel data pemesan_mobil
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(new Color(224, 242, 241));

        // Inisialisasi dan tambahkan DataTablePanel dan DataPelangganPanel
        dataTablePanel = new DataTablePanel();
        dataPelangganPanel = new DataPelangganPanel();
        dataMobilPanel = new DataMobilPanel();
        dataSopirPanel = new DataSopirPanel();
        contentArea.add(dataTablePanel, BorderLayout.CENTER);
        add(contentArea, BorderLayout.CENTER);
    }


        private boolean showLoginDialog(){
            JDialog loginDialog = new JDialog(this, "Login", true);
            loginDialog.setSize(300, 200);
            loginDialog.setLocationRelativeTo(this);
            loginDialog.setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JTextField usernameField = new JTextField(15);
            JPasswordField passwordField = new JPasswordField(15);

            gbc.gridx = 0;
            gbc.gridy = 0;
            loginDialog.add(new JLabel("Username:"), gbc);

            gbc.gridx = 1;
            loginDialog.add(usernameField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            loginDialog.add(new JLabel("Password:"), gbc);

            gbc.gridx = 1;
            loginDialog.add(passwordField, gbc);

            JButton loginButton = new JButton("Login");
            JButton cancelButton = new JButton("Cancel");

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(loginButton);
            buttonPanel.add(cancelButton);

            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            loginDialog.add(buttonPanel, gbc);


            final boolean[] isLoggedIn = {false};

            loginButton.addActionListener(e -> {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (authenticate(username, password)) {
                    isLoggedIn[0] = true;
                    loginDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(loginDialog, "Username atau password salah.",
                            "Login Gagal", JOptionPane.ERROR_MESSAGE);
                }
            });

            cancelButton.addActionListener(e -> System.exit(0));

            loginDialog.setVisible(true);
            return isLoggedIn[0];
        }


    private boolean authenticate(String username, String password) {
        if (!isValidEmail(username)) {
            JOptionPane.showMessageDialog(null, "Username harus dalam format email yang valid.",
                    "Invalid input", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        boolean isLoginSuccesfull = DatabaseManager.getInstance().checkLogin(username, password);

        if (!isLoginSuccesfull) {
            JOptionPane.showMessageDialog(null, "Username atau Password salah.",
                    "Login Gagal",JOptionPane.ERROR_MESSAGE);
        }
        return isLoginSuccesfull;
    }

    private boolean isValidEmail(String email){
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        return Pattern.matches(emailRegex, email);

    }


    private JMenu createMenu(String title, Font font, java.awt.event.ActionListener action) {
        JMenu menu = new JMenu(title);
        menu.setFont(font);
        menu.setForeground(Color.WHITE);
        if (action != null) {
            menu.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    action.actionPerformed(null);
                }
            });
        }
        return menu;
    }

    private JMenu createStyledMenu(String title, Font font, java.awt.event.ActionListener action) {
        JMenu menu = new JMenu(title);
        menu.setFont(font);
        menu.setForeground(Color.WHITE);
        menu.setBackground(Color.CYAN);

        // Hover effects
        menu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                menu.setBackground(Color.BLUE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                menu.setBackground(Color.CYAN);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                action.actionPerformed(null);
            }
        });

        return menu;
    }

    private void showHome() {
        contentArea.removeAll();
        contentArea.add(dataTablePanel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();

        // Ganti menu dinamis ke "Tambah Pemesanan" saat berada di Home
        switchToMenu(tambahPemesanMenu);
    }

    private void showDataPelanggan() {
        contentArea.removeAll();
        contentArea.add(dataPelangganPanel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();

        // Ganti menu dinamis ke "Tambah Pelanggan" saat berada di Data Pelanggan
        switchToMenu(tambahPelangganMenu);
    }

    private void openAddCustomerDialog() {
        new AddCustomerDialog(this, dataPelangganPanel).setVisible(true);
    }

    // Show DataMobil Panel
    private void showDataMobil() {
        contentArea.removeAll();
        contentArea.add(dataMobilPanel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();

        // Switch dynamic menu to "Tambah Mobil" when on Data Mobil
        switchToMenu(tambahMobilMenu);
    }

    // Open AddCarDialog
    private void openAddCarDialog() {
        new AddCarDialog(this, dataMobilPanel).setVisible(true);
    }



    // Show DataSopir Panel
    private void showDataSopir() {
        contentArea.removeAll();
        contentArea.add(dataSopirPanel, BorderLayout.CENTER);  // Use dataSopirPanel
        contentArea.revalidate();
        contentArea.repaint();

        // Switch dynamic menu to "Tambah Sopir" when on Data Sopir
        switchToMenu(tambahSopirMenu);
    }

    // Open AddDriverDialog
    private void openAddDriverDialog() {
        new AddDriverDialog(this, dataSopirPanel).setVisible(true);  // Use dataSopirPanel
    }


    private void openAddOrderDialog() {
        Object[] emptyData = new Object[]{"", "", "", "", "", "", "", "", "", ""};
        EditDialog addDialog = new EditDialog(this, emptyData, DatabaseManager.getInstance(), -1, dataTablePanel, false);
        addDialog.setVisible(true);
    }

    // Metode untuk mengganti menu dinamis
    private void switchToMenu(JMenu menu) {
        menuBar.remove(tambahPelangganMenu);
        menuBar.remove(tambahPemesanMenu);
        menuBar.remove(tambahMobilMenu);
        menuBar.remove(tambahSopirMenu);
        menuBar.add(menu);  // Tambahkan menu dinamis baru di akhir
        menuBar.revalidate();
        menuBar.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
