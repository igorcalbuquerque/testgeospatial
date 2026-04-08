package com.sccon.geospatial.model;

import java.time.LocalDate;

public class Person {

    private Integer id;
    private String name;
    private LocalDate birthDate;
    private LocalDate admissionDate;

    public Person(Integer id, String name, LocalDate birthDate, LocalDate admissionDate) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.admissionDate = admissionDate;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public LocalDate getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDate admissionDate) { this.admissionDate = admissionDate; }
}
