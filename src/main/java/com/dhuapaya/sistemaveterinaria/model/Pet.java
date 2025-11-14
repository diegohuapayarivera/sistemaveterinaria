package com.dhuapaya.sistemaveterinaria.model;

import java.sql.Date;

public class Pet {
    private Integer id;
    private Integer clientId;
    private String name;
    private String species;
    private String breed;
    private Date birthdate;

    public Pet() {
    }

    public Pet(Integer id, Integer clientId, String name, String species, String breed, Date birthdate) {
        this.id = id;
        this.clientId = clientId;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.birthdate = birthdate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }
}
