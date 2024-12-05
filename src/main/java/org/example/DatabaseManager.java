package org.example;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static DatabaseManager instance;
    private static final String URL = "jdbc:mysql://localhost:3306/db_pbo_pemesanan_mobil";//silahkan isi sendiri untuk URL koneksi database ini
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    // Private constructor to prevent instantiation
    private DatabaseManager() {}

    // Method to get the singleton instance
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // Koneksi database
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Ambil data dari tabel pemesan_mobil
    public ResultSet fetchData() {
        String query = "SELECT * FROM pemesan_mobil ORDER BY id DESC";
        return executeQuery(query);
    }

    // Ambil data dari tabel mobil
    public ResultSet fetchMobilData() {
        String query = "SELECT * FROM mobil ORDER BY id DESC";
        return executeQuery(query);
    }

    // Ambil data dari tabel pelanggan
    public ResultSet fetchPelangganData() {
        String query = "SELECT id, nama_pelanggan, nomor_telepon, alamat, email, created_at FROM pelanggan ORDER BY id DESC";
        return executeQuery(query);
    }

    // Ambil data dari tabel sopir
    public ResultSet fetchSopirData() {
        String query = "SELECT * FROM sopir ORDER BY id DESC";
        return executeQuery(query);
    }

    // Method to execute a query and return ResultSet
    public ResultSet executeQuery(String query) {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception
            return null; // Return null or handle the error as needed
        }
    }

    // Mengambil data pelanggan sebagai list untuk pengelolaan yang lebih aman
    public List<Object[]> fetchPelangganDataAsList() {
        String query = "SELECT id, nama_pelanggan, nomor_telepon, alamat, email, created_at FROM pelanggan ORDER BY id DESC";
        List<Object[]> dataList = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                dataList.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nama_pelanggan"),
                        rs.getString("nomor_telepon"),
                        rs.getString("alamat"),
                        rs.getString("email"),
                        rs.getTimestamp("created_at")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal mengambil data pelanggan.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        return dataList;
    }

    public void deleteMobilById(int id) throws SQLException {
        String query = "DELETE FROM mobil WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Data mobil dengan ID " + id + " berhasil dihapus.");
        } catch (SQLException e) {
            System.err.println("Gagal menghapus data mobil dengan ID " + id);
            throw e;
        }
    }


    public void deletePelangganById(int id) throws SQLException {
        String query = "DELETE FROM pelanggan WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Data pelanggan dengan ID " + id + " berhasil dihapus.");
        } catch (SQLException e) {
            System.err.println("Gagal menghapus data pelanggan dengan ID " + id);
            throw e;
        }
    }


    public void deleteSopirById(int id) throws SQLException {
        String query = "DELETE FROM sopir WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Data sopir dengan ID " + id + " berhasil dihapus.");
        } catch (SQLException e) {
            System.err.println("Gagal menghapus data sopir dengan ID " + id);
            throw e;
        }
    }


    public void deleteDataById(int id) throws SQLException {
        String query = "DELETE FROM pemesan_mobil WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Data pemesanan dengan ID " + id + " berhasil dihapus.");
        } catch (SQLException e) {
            System.err.println("Gagal menghapus data pemesanan dengan ID " + id);
            throw e;
        }
    }




    // Update data
    public int updateData(String query, Object[] params) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception
            return 0; // Return 0 or handle the error as needed
        }
    }

    public boolean checkLogin(String username, String password) {
        String query = "SELECT COUNT(*) FROM admin WHERE username = ? AND password = ?";

        try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(query)) {



            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()){
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal memeriksa login.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
}
