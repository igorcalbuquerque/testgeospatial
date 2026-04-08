package com.sccon.geospatial.exception;

public class InvalidOutputFormatException extends RuntimeException {

    public InvalidOutputFormatException(String received, String validValues) {
        super("Invalid output format '" + received + "'. Valid values: " + validValues);
    }
}
