package com.dhuapaya.sistemaveterinaria.dao;

import com.dhuapaya.sistemaveterinaria.db.H2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDao {

    public boolean validateCredentials(String username, String password) throws Exception {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND password = ?";
        try (Connection cn = H2.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Devuelve true si las credenciales son válidas
                }
            }
        }
        return false;
    }
}