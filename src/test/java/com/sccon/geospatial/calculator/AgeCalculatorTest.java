package com.sccon.geospatial.calculator;

import com.sccon.geospatial.exception.InvalidOutputFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class AgeCalculatorTest {

    private AgeCalculator calculator;

    private static final LocalDate BIRTH_DATE = LocalDate.of(2000, 4, 6);

    @BeforeEach
    void setUp() {
        calculator = new AgeCalculator();
    }

    @Test
    void calculate_days_returnsCorrectValue() {
        LocalDate today = LocalDate.now();
        long expected = ChronoUnit.DAYS.between(BIRTH_DATE, today);

        assertEquals(expected, calculator.calculate(BIRTH_DATE, today, "days"));
    }

    @Test
    void calculate_months_returnsCorrectValue() {
        LocalDate today = LocalDate.now();
        long expected = ChronoUnit.MONTHS.between(BIRTH_DATE, today);

        assertEquals(expected, calculator.calculate(BIRTH_DATE, today, "months"));
    }

    @Test
    void calculate_years_returnsCorrectValue() {
        LocalDate today = LocalDate.now();
        long expected = ChronoUnit.YEARS.between(BIRTH_DATE, today);

        assertEquals(expected, calculator.calculate(BIRTH_DATE, today, "years"));
    }

    @Test
    void calculate_outputIsCaseInsensitive() {
        LocalDate today = LocalDate.now();
        long expected = ChronoUnit.YEARS.between(BIRTH_DATE, today);

        assertEquals(expected, calculator.calculate(BIRTH_DATE, today, "YEARS"));
        assertEquals(expected, calculator.calculate(BIRTH_DATE, today, "Years"));
    }

    @Test
    void calculate_invalidOutput_throwsInvalidOutputFormatException() {
        assertThrows(InvalidOutputFormatException.class,
                () -> calculator.calculate(BIRTH_DATE, LocalDate.now(), "weeks"));
    }

    @Test
    void calculate_sameDay_returnsZero() {
        LocalDate date = LocalDate.of(1990, 1, 1);

        assertEquals(0, calculator.calculate(date, date, "days"));
        assertEquals(0, calculator.calculate(date, date, "months"));
        assertEquals(0, calculator.calculate(date, date, "years"));
    }
}
