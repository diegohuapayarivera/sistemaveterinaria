package com.dhuapaya.sistemaveterinaria.dao;

import com.dhuapaya.sistemaveterinaria.db.H2;
import com.dhuapaya.sistemaveterinaria.model.Pet;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PetDao implements CrudDao<Pet> {

    @Override
    public void create(Pet p) throws Exception {
        String sql = "INSERT INTO pets(client_id, name, species, breed, birthdate) VALUES(?, ?, ?, ?, ?)";
        try (Connection cn = H2.get();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, p.getClientId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getSpecies());
            ps.setString(4, p.getBreed());
            if (p.getBirthdate() != null) {
                ps.setDate(5, p.getBirthdate());
            } else {
                ps.setDate(5, null);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1)); // asignamos el ID generado
                }
            }
        }
    }

    @Override
    public List<Pet> listAll() throws Exception {
        String sql = "SELECT id, client_id, name, species, breed, birthdate FROM pets ORDER BY id DESC";
        try (Connection cn = H2.get();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Pet> out = new ArrayList<>();
            while (rs.next()) {
                Pet p = new Pet(
                        rs.getInt("id"),
                        rs.getInt("client_id"),
                        rs.getString("name"),
                        rs.getString("species"),
                        rs.getString("breed"),
                        rs.getDate("birthdate")
                );
                out.add(p);
            }
            return out;
        }
    }

    @Override
    public void update(Pet p) throws Exception {
        String sql = "UPDATE pets SET client_id=?, name=?, species=?, breed=?, birthdate=? WHERE id=?";
        try (Connection cn = H2.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, p.getClientId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getSpecies());
            ps.setString(4, p.getBreed());
            if (p.getBirthdate() != null) {
                ps.setDate(5, p.getBirthdate());
            } else {
                ps.setDate(5, null);
            }
            ps.setInt(6, p.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws Exception {
        String sql = "DELETE FROM pets WHERE id=?";
        try (Connection cn = H2.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}