package com.dhuapaya.sistemaveterinaria.dao;

import com.dhuapaya.sistemaveterinaria.db.H2;
import com.dhuapaya.sistemaveterinaria.model.Pet;

import java.io.FileWriter;
import java.io.PrintWriter;
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


    public void exportToCsv(String filePath) throws Exception {
        String sql = """
        SELECT p.name AS pet_name, p.species, p.breed, p.birthdate, 
               c.name AS client_name, c.last_name AS client_last_name
        FROM pets p
        JOIN clients c ON p.client_id = c.id
    """;

        try (Connection cn = H2.get();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
             PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {

            // Escribir encabezados
            writer.println("Mascota,Especie,Raza,Fecha de Nacimiento,Cliente");

            // Escribir filas
            while (rs.next()) {
                String petName = rs.getString("pet_name");
                String species = rs.getString("species");
                String breed = rs.getString("breed");
                Date birthdate = rs.getDate("birthdate");
                String clientFullName = rs.getString("client_name") + " " + rs.getString("client_last_name");

                writer.printf("%s,%s,%s,%s,%s%n",
                        petName,
                        species,
                        breed,
                        (birthdate != null ? birthdate.toString() : ""),
                        clientFullName);
            }
        }
    }
}