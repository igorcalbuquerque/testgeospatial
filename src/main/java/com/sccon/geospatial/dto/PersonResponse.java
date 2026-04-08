package com.sccon.geospatial.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sccon.geospatial.model.Person;

import java.time.LocalDate;

public class PersonResponse {

    private Integer id;
    private String name;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate birthDate;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate admissionDate;

    private PersonResponse() {}

    public static PersonResponse from(Person person) {
        PersonResponse response = new PersonResponse();
        response.id = person.getId();
        response.name = person.getName();
        response.birthDate = person.getBirthDate();
        response.admissionDate = person.getAdmissionDate();
        return response;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public LocalDate getBirthDate() { return birthDate; }
    public LocalDate getAdmissionDate() { return admissionDate; }
}
