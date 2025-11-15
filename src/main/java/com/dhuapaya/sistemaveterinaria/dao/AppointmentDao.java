package com.dhuapaya.sistemaveterinaria.dao;

import com.dhuapaya.sistemaveterinaria.db.H2;
import com.dhuapaya.sistemaveterinaria.model.Appointment;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDao implements CrudDao<Appointment> {

    @Override
    public void create(Appointment a) throws Exception {
        String sql = "INSERT INTO appointments(pet_id, date_time, reason, notes, status) VALUES(?, ?, ?, ?, ?)";
        try (Connection cn = H2.get();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, a.getPetId());
            ps.setTimestamp(2, Timestamp.valueOf(a.getDateTime()));
            ps.setString(3, a.getReason());
            ps.setString(4, a.getNotes());
            ps.setString(5, a.getStatus());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    a.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public List<Appointment> listAll() throws Exception {
        String sql = "SELECT id, pet_id, date_time, reason, notes, status FROM appointments ORDER BY date_time DESC";

        try (Connection cn = H2.get();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Appointment> out = new ArrayList<>();

            while (rs.next()) {
                out.add(new Appointment(
                        rs.getInt("id"),
                        rs.getInt("pet_id"),
                        rs.getTimestamp("date_time").toLocalDateTime(),
                        rs.getString("reason"),
                        rs.getString("notes"),
                        rs.getString("status")
                ));
            }
            return out;
        }
    }

    @Override
    public void update(Appointment a) throws Exception {
        String sql = "UPDATE appointments SET pet_id=?, date_time=?, reason=?, notes=?, status=? WHERE id=?";

        try (Connection cn = H2.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, a.getPetId());
            ps.setTimestamp(2, Timestamp.valueOf(a.getDateTime()));
            ps.setString(3, a.getReason());
            ps.setString(4, a.getNotes());
            ps.setString(5, a.getStatus());
            ps.setInt(6, a.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws Exception {
        String sql = "DELETE FROM appointments WHERE id=?";

        try (Connection cn = H2.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Appointment> listByDateRangeAndStatus(LocalDateTime from, LocalDateTime to, String status) throws Exception {
        StringBuilder sql = new StringBuilder(
                "SELECT id, pet_id, date_time, reason, notes, status FROM appointments WHERE 1=1"
        );
        var params = new ArrayList<Object>();

        if (from != null) {
            sql.append(" AND date_time >= ?");
            params.add(Timestamp.valueOf(from));
        }
        if (to != null) {
            sql.append(" AND date_time <= ?");
            params.add(Timestamp.valueOf(to));
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND status = ?");
            params.add(status);
        }

        sql.append(" ORDER BY date_time DESC");

        try (Connection cn = H2.get();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {

            // setear parámetros en orden
            int index = 1;
            for (Object param : params) {
                if (param instanceof Timestamp ts) {
                    ps.setTimestamp(index++, ts);
                } else if (param instanceof String s) {
                    ps.setString(index++, s);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<Appointment> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new Appointment(
                            rs.getInt("id"),
                            rs.getInt("pet_id"),
                            rs.getTimestamp("date_time").toLocalDateTime(),
                            rs.getString("reason"),
                            rs.getString("notes"),
                            rs.getString("status")
                    ));
                }
                return out;
            }
        }
    }


    public void exportToCsv(String filePath) throws Exception {
        String sql = """
        SELECT a.date_time, a.reason, a.notes, a.status,
               p.name AS pet_name,
               c.name AS client_name, c.last_name AS client_last_name
        FROM appointments a
        JOIN pets p ON a.pet_id = p.id
        JOIN clients c ON p.client_id = c.id
    """;

        try (Connection cn = H2.get();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
             PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {

            // Escribir encabezados
            writer.println("Fecha y Hora,Razon,Notas,Estado,Mascota,Cliente");

            // Escribir filas
            while (rs.next()) {
                String dateTime = rs.getTimestamp("date_time").toLocalDateTime().toString();
                String reason = rs.getString("reason");
                String notes = rs.getString("notes");
                String status = rs.getString("status");
                String petName = rs.getString("pet_name");
                String clientFullName = rs.getString("client_name") + " " + rs.getString("client_last_name");

                writer.printf("%s,%s,%s,%s,%s,%s%n",
                        dateTime,
                        reason,
                        notes,
                        status,
                        petName,
                        clientFullName);
            }
        }
    }
}