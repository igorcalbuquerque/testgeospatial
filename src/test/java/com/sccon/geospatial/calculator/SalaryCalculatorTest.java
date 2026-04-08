package com.sccon.geospatial.calculator;

import com.sccon.geospatial.exception.InvalidOutputFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class SalaryCalculatorTest {

    private SalaryCalculator calculator;

    private static final BigDecimal INITIAL_SALARY = new BigDecimal("1558.00");
    private static final BigDecimal ANNUAL_RAISE   = new BigDecimal("1.18");
    private static final BigDecimal ANNUAL_BONUS   = new BigDecimal("500.00");
    private static final BigDecimal MINIMUM_WAGE   = new BigDecimal("1302.00");

    @BeforeEach
    void setUp() {
        calculator = new SalaryCalculator();
    }

    private BigDecimal expectedFull(LocalDate admissionDate, LocalDate referenceDate) {
        long years = ChronoUnit.YEARS.between(admissionDate, referenceDate);
        BigDecimal salary = INITIAL_SALARY;
        for (long i = 0; i < years; i++) {
            salary = salary.multiply(ANNUAL_RAISE).add(ANNUAL_BONUS);
        }
        return salary.setScale(2, RoundingMode.CEILING);
    }

    private BigDecimal expectedMin(LocalDate admissionDate, LocalDate referenceDate) {
        long years = ChronoUnit.YEARS.between(admissionDate, referenceDate);
        BigDecimal salary = INITIAL_SALARY;
        for (long i = 0; i < years; i++) {
            salary = salary.multiply(ANNUAL_RAISE).add(ANNUAL_BONUS);
        }
        return salary.divide(MINIMUM_WAGE, 10, RoundingMode.HALF_UP)
                     .setScale(2, RoundingMode.CEILING);
    }

    @Test
    void calculate_full_zeroYears_returnsInitialSalary() {
        LocalDate admissionDate = LocalDate.now();
        BigDecimal result = calculator.calculate(admissionDate, admissionDate, "full");

        assertEquals(INITIAL_SALARY.setScale(2, RoundingMode.CEILING), result);
    }

    @Test
    void calculate_full_afterOneYear_returnsCorrectValue() {
        LocalDate admissionDate = LocalDate.now().minusYears(1);
        LocalDate referenceDate = LocalDate.now();

        assertEquals(expectedFull(admissionDate, referenceDate),
                     calculator.calculate(admissionDate, referenceDate, "full"));
    }

    @Test
    void calculate_full_dynamicYears_returnsCorrectValue() {
        LocalDate admissionDate = LocalDate.of(2020, 5, 10);
        LocalDate referenceDate = LocalDate.now();

        assertEquals(expectedFull(admissionDate, referenceDate),
                     calculator.calculate(admissionDate, referenceDate, "full"));
    }

    @Test
    void calculate_min_dynamicYears_returnsCorrectValue() {
        LocalDate admissionDate = LocalDate.of(2020, 5, 10);
        LocalDate referenceDate = LocalDate.now();

        assertEquals(expectedMin(admissionDate, referenceDate),
                     calculator.calculate(admissionDate, referenceDate, "min"));
    }

    @Test
    void calculate_outputIsCaseInsensitive() {
        LocalDate admissionDate = LocalDate.of(2020, 5, 10);
        LocalDate referenceDate = LocalDate.now();

        assertEquals(
                calculator.calculate(admissionDate, referenceDate, "full"),
                calculator.calculate(admissionDate, referenceDate, "FULL")
        );
    }

    @Test
    void calculate_invalidOutput_throwsInvalidOutputFormatException() {
        assertThrows(InvalidOutputFormatException.class,
                () -> calculator.calculate(LocalDate.of(2020, 1, 1), LocalDate.now(), "hourly"));
    }

    @Test
    void calculate_ceilingRounding_isApplied() {
        LocalDate admissionDate = LocalDate.now().minusYears(2);
        BigDecimal result = calculator.calculate(admissionDate, LocalDate.now(), "full");

        assertEquals(2, result.scale());

        BigDecimal raw = INITIAL_SALARY
                .multiply(ANNUAL_RAISE).add(ANNUAL_BONUS)
                .multiply(ANNUAL_RAISE).add(ANNUAL_BONUS);
        assertTrue(result.compareTo(raw.setScale(2, RoundingMode.FLOOR)) >= 0);
    }
}
