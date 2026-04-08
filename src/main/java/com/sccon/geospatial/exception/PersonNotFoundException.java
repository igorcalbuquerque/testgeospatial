package com.sccon.geospatial.exception;

public class PersonNotFoundException extends RuntimeException {

    public PersonNotFoundException(Integer id) {
        super("Person with id " + id + " not found");
    }
}
