package com.dhuapaya.sistemaveterinaria.dao;

import com.dhuapaya.sistemaveterinaria.db.H2;
import com.dhuapaya.sistemaveterinaria.model.Client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClientDao implements CrudDao<Client> {

    @Override
    public void create(Client c) throws Exception {
        String sql = "INSERT INTO clients(name, last_name, phone, email, dni) VALUES(?, ?, ?, ?, ?)";
        try (Connection cn = H2.get();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getName());
            ps.setString(2, c.getLastName());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getDni());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1)); // asignamos el ID generado al objeto
                }
            }
        }
    }

    @Override
    public List<Client> listAll() throws Exception {
        String sql = "SELECT id, name, last_name, phone, email, dni FROM clients ORDER BY id DESC";
        try (Connection cn = H2.get();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Client> out = new ArrayList<>();
            while (rs.next()) {
                Client c = new Client(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("last_name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("dni")
                );
                out.add(c);
            }
            return out;
        }
    }

    @Override
    public void update(Client c) throws Exception {
        String sql = "UPDATE clients SET name=?, last_name=?, phone=?, email=?, dni=? WHERE id=?";
        try (Connection cn = H2.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, c.getName());
            ps.setString(2, c.getLastName());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getDni());
            ps.setInt(6, c.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws Exception {
        String sql = "DELETE FROM clients WHERE id=?";
        try (Connection cn = H2.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Método extra fuera del CRUD genérico (no está en la interfaz)
    public boolean isDniUnique(String dni) throws Exception {
        String sql = "SELECT COUNT(*) FROM clients WHERE dni = ?";
        try (Connection cn = H2.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count == 0;
                }
            }
        }
        return true;
    }
}