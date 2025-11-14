package com.dhuapaya.sistemaveterinaria.model;

import java.time.LocalDateTime;

public class Appointment {
    private Integer id;
    private Integer petId;
    private LocalDateTime dateTime;
    private String reason;
    private String notes;
    private String status;

    public Appointment() {}

    public Appointment(Integer id, Integer petId, LocalDateTime dateTime, String reason, String notes, String status) {
        this.id = id;
        this.petId = petId;
        this.dateTime = dateTime;
        this.reason = reason;
        this.notes = notes;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPetId() {
        return petId;
    }

    public void setPetId(Integer petId) {
        this.petId = petId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}