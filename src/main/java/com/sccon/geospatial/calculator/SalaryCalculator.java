package com.sccon.geospatial.calculator;

import com.sccon.geospatial.exception.InvalidOutputFormatException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class SalaryCalculator {

    private static final BigDecimal INITIAL_SALARY    = new BigDecimal("1558.00");
    private static final BigDecimal ANNUAL_RAISE      = new BigDecimal("1.18");
    private static final BigDecimal ANNUAL_BONUS      = new BigDecimal("500.00");
    private static final BigDecimal MINIMUM_WAGE      = new BigDecimal("1302.00");
    private static final String     VALID_OUTPUT_VALUES = "min, full";

    public BigDecimal calculate(LocalDate admissionDate, LocalDate referenceDate, String output) {
        long yearsAtCompany = ChronoUnit.YEARS.between(admissionDate, referenceDate);
        BigDecimal rawSalary = computeRawSalary(yearsAtCompany);

        return switch (output.toLowerCase()) {
            case "full" -> rawSalary.setScale(2, RoundingMode.CEILING);
            case "min"  -> rawSalary
                               .divide(MINIMUM_WAGE, 10, RoundingMode.HALF_UP)
                               .setScale(2, RoundingMode.CEILING);
            default     -> throw new InvalidOutputFormatException(output, VALID_OUTPUT_VALUES);
        };
    }

    private BigDecimal computeRawSalary(long years) {
        BigDecimal salary = INITIAL_SALARY;
        for (long i = 0; i < years; i++) {
            salary = salary.multiply(ANNUAL_RAISE).add(ANNUAL_BONUS);
        }
        return salary;
    }
}
