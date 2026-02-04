/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.proteanit.sql.DbUtils;

/**
 *
 * @author USER4
 */
public class config {
    
private String safe(String value) {
    return (value == null || value.trim().isEmpty()) ? "" : value.trim();
}
    

public static class Session {
    public static int userId;
    public static String username;
    public static String fullname;
    public static String lastName;
    public static String role;

    public static void clear() {
        userId = 0;
        username = null;
        fullname = null;
        lastName = null;
        role = null;
    }
}

    
    //Connection Method to SQLITE
public static Connection connectDB() {
        Connection con = null;
        try {
            Class.forName("org.sqlite.JDBC"); // Load the SQLite JDBC driver
            con = DriverManager.getConnection("jdbc:sqlite:pdrDB.db"); // Establish connection
            System.out.println("Connection Successful");
        } catch (Exception e) {
            System.out.println("Connection Failed: " + e);
        }
        return con;
    }

public void addRecord(String sql, Object... values) {
    try (Connection conn = this.connectDB(); // Use the connectDB method
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        // Loop through the values and set them in the prepared statement dynamically
        for (int i = 0; i < values.length; i++) {
            if (values[i] instanceof Integer) {
                pstmt.setInt(i + 1, (Integer) values[i]); // If the value is Integer
            } else if (values[i] instanceof Double) {
                pstmt.setDouble(i + 1, (Double) values[i]); // If the value is Double
            } else if (values[i] instanceof Float) {
                pstmt.setFloat(i + 1, (Float) values[i]); // If the value is Float
            } else if (values[i] instanceof Long) {
                pstmt.setLong(i + 1, (Long) values[i]); // If the value is Long
            } else if (values[i] instanceof Boolean) {
                pstmt.setBoolean(i + 1, (Boolean) values[i]); // If the value is Boolean
            } else if (values[i] instanceof java.util.Date) {
                pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) values[i]).getTime())); // If the value is Date
            } else if (values[i] instanceof java.sql.Date) {
                pstmt.setDate(i + 1, (java.sql.Date) values[i]); // If it's already a SQL Date
            } else if (values[i] instanceof java.sql.Timestamp) {
                pstmt.setTimestamp(i + 1, (java.sql.Timestamp) values[i]); // If the value is Timestamp
            } else {
                pstmt.setString(i + 1, values[i].toString()); // Default to String for other types
            }
        }

        pstmt.executeUpdate();
        System.out.println("Record added successfully!");
    } catch (SQLException e) {
        System.out.println("Error adding record: " + e.getMessage());
    }
}

public boolean login(String username, String password, String role, String status) {

    String sql = "SELECT * FROM tbl_accounts WHERE u_name=? AND confirm_pass=? AND role=? AND account_status=?";

    try (Connection conn = connectDB();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, username);
        ps.setString(2, password);
        ps.setString(3, role);
        ps.setString(4, status);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            // SAVE SESSION DATA
            Session.userId = rs.getInt("u_id"); // adjust column name if needed
            Session.username = rs.getString("u_name");

            // Combine name from registration
            String f = safe(rs.getString("f_name"));
            String m = safe(rs.getString("m_name"));
            String l = safe(rs.getString("l_name"));

            Session.fullname = String.join(" ", f, m, l).trim();
            if (Session.fullname.isEmpty()) {
                Session.fullname = "Anonymous";
            }

            Session.lastName = l.isEmpty() ? "Anonymous" : l;

            return true;
        }

    } catch (SQLException e) {
        System.out.println("Login Error: " + e.getMessage());
    }
    return false;
}

public String loginWithValidation(String username, String password) {
    String sqlUser = "SELECT * FROM tbl_accounts WHERE u_name=?";
    
    try (Connection conn = connectDB();
         PreparedStatement ps = conn.prepareStatement(sqlUser)) {
        
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        
        if (!rs.next()) {
            return "User does not exist"; // username not found
        }
        
        String dbPass = rs.getString("confirm_pass");
        String accountStatus = rs.getString("account_status");
        String role = rs.getString("role");
        
        if (!dbPass.equals(password)) {
            return "Invalid password"; // password mismatch
        }
        
        if (!accountStatus.equalsIgnoreCase("Active")) {
            return "Deactivated"; // account no longer active
        }
        
        // Save session data if everything is correct
        Session.userId = rs.getInt("u_id");
        Session.username = rs.getString("u_name");
        String f = safe(rs.getString("f_name"));
        String m = safe(rs.getString("m_name"));
        String l = safe(rs.getString("l_name"));

        Session.fullname = String.join(" ", f, m, l).trim();

        if (Session.fullname.isEmpty()) {
            Session.fullname = "Anonymous";
        }
        
        Session.lastName = l.isEmpty() ? "Anonymous" : l;
        Session.role = role;
        
        return role; // return the role if login successful
        
    } catch (SQLException e) {
        System.out.println("Login Error: " + e.getMessage());
        return "Error";
    }
}


public void displayData(String sql, javax.swing.JTable table) {
    try (Connection conn = connectDB();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {
        
        // This line automatically maps the Resultset to your JTable
        table.setModel(DbUtils.resultSetToTableModel(rs));
        
    } catch (SQLException e) {
        System.out.println("Error displaying data: " + e.getMessage());
    }
}


}
