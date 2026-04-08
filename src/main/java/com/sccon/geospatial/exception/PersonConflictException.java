package com.sccon.geospatial.exception;

public class PersonConflictException extends RuntimeException {

    public PersonConflictException(Integer id) {
        super("Person with id " + id + " already exists");
    }
}
