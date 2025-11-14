package com.dhuapaya.sistemaveterinaria.model;

public class Client extends AbstractPerson {

    private Integer id;
    private String dni;

    public Client() {
        super(null, null, null, null);
    }

    public Client(Integer id, String name, String lastName,
                  String phone, String email, String dni) {
        super(name, lastName, phone, email); // inicializa correctamente AbstractPerson
        this.id = id;
        this.dni = dni;
    }

    // --- ID ---
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    // --- DNI ---
    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

}