package com.sccon.geospatial.calculator;

import com.sccon.geospatial.exception.InvalidOutputFormatException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class AgeCalculator {

    private static final String VALID_OUTPUT_VALUES = "days, months, years";

    public long calculate(LocalDate birthDate, LocalDate referenceDate, String output) {
        return switch (output.toLowerCase()) {
            case "days"   -> ChronoUnit.DAYS.between(birthDate, referenceDate);
            case "months" -> ChronoUnit.MONTHS.between(birthDate, referenceDate);
            case "years"  -> ChronoUnit.YEARS.between(birthDate, referenceDate);
            default       -> throw new InvalidOutputFormatException(output, VALID_OUTPUT_VALUES);
        };
    }
}
