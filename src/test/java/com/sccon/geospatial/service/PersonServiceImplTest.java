package com.sccon.geospatial.service;

import com.sccon.geospatial.calculator.AgeCalculator;
import com.sccon.geospatial.calculator.SalaryCalculator;
import com.sccon.geospatial.dto.PersonPatchRequest;
import com.sccon.geospatial.dto.PersonRequest;
import com.sccon.geospatial.dto.PersonResponse;
import com.sccon.geospatial.exception.InvalidPersonDataException;
import com.sccon.geospatial.exception.PersonConflictException;
import com.sccon.geospatial.exception.PersonNotFoundException;
import com.sccon.geospatial.model.Person;
import com.sccon.geospatial.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceImplTest {

    @Mock private PersonRepository repository;
    @Mock private AgeCalculator ageCalculator;
    @Mock private SalaryCalculator salaryCalculator;

    @InjectMocks private PersonServiceImpl service;

    private Person jose;

    @BeforeEach
    void setUp() {
        jose = new Person(1, "José da Silva", LocalDate.of(2000, 4, 6), LocalDate.of(2020, 5, 10));
    }

    // ── findAll ──────────────────────────────────────────────────────────────

    @Test
    void findAll_returnsSortedByName() {
        Person ana    = new Person(2, "Ana Costa",     LocalDate.of(1990, 1, 1), LocalDate.of(2015, 1, 1));
        Person carlos = new Person(3, "Carlos Mendes", LocalDate.of(1985, 1, 1), LocalDate.of(2010, 1, 1));

        when(repository.findAll()).thenReturn(List.of(carlos, jose, ana));

        List<PersonResponse> result = service.findAll();

        assertEquals("Ana Costa",     result.get(0).getName());
        assertEquals("Carlos Mendes", result.get(1).getName());
        assertEquals("José da Silva", result.get(2).getName());
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_found_returnsResponse() {
        when(repository.findById(1)).thenReturn(Optional.of(jose));

        PersonResponse response = service.findById(1);

        assertEquals(jose.getId(),   response.getId());
        assertEquals(jose.getName(), response.getName());
    }

    @Test
    void findById_notFound_throwsPersonNotFoundException() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThrows(PersonNotFoundException.class, () -> service.findById(99));
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void create_withoutId_savesAndReturnsResponse() {
        PersonRequest request = buildRequest(null, "Maria Lima",
                LocalDate.of(1995, 8, 15), LocalDate.of(2019, 3, 1));

        Person saved = new Person(4, request.getName(), request.getBirthDate(), request.getAdmissionDate());
        when(repository.save(any())).thenReturn(saved);

        PersonResponse response = service.create(request);

        assertEquals(saved.getId(),   response.getId());
        assertEquals(saved.getName(), response.getName());
        verify(repository, never()).existsById(any());
    }

    @Test
    void create_withExistingId_throwsPersonConflictException() {
        PersonRequest request = buildRequest(1, "Maria Lima",
                LocalDate.of(1995, 8, 15), LocalDate.of(2019, 3, 1));

        when(repository.existsById(1)).thenReturn(true);

        assertThrows(PersonConflictException.class, () -> service.create(request));
        verify(repository, never()).save(any());
    }

    @Test
    void create_admissionBeforeBirth_throwsIllegalArgumentException() {
        PersonRequest request = buildRequest(null, "Teste",
                LocalDate.of(2000, 1, 1), LocalDate.of(1999, 1, 1));

        assertThrows(IllegalArgumentException.class, () -> service.create(request));
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_found_replacesAndReturnsResponse() {
        PersonRequest request = buildRequest(null, "José Atualizado",
                LocalDate.of(2000, 4, 6), LocalDate.of(2020, 5, 10));

        when(repository.existsById(1)).thenReturn(true);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PersonResponse response = service.update(1, request);

        assertEquals("José Atualizado", response.getName());
    }

    @Test
    void update_notFound_throwsPersonNotFoundException() {
        PersonRequest request = buildRequest(null, "X",
                LocalDate.of(2000, 1, 1), LocalDate.of(2020, 1, 1));

        when(repository.existsById(99)).thenReturn(false);

        assertThrows(PersonNotFoundException.class, () -> service.update(99, request));
    }

    // ── patch ────────────────────────────────────────────────────────────────

    @Test
    void patch_name_updatesOnlyName() {
        when(repository.findById(1)).thenReturn(Optional.of(jose));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PersonPatchRequest request = buildPatch("José Silva Atualizado", null, null);
        PersonResponse response = service.patch(1, request);

        assertEquals("José Silva Atualizado", response.getName());
        assertEquals(jose.getBirthDate(),     response.getBirthDate());
    }

    @Test
    void patch_blankName_throwsIllegalArgumentException() {
        when(repository.findById(1)).thenReturn(Optional.of(jose));

        PersonPatchRequest request = buildPatch("   ", null, null);

        assertThrows(IllegalArgumentException.class, () -> service.patch(1, request));
    }

    @Test
    void patch_emptyBody_throwsIllegalArgumentException() {
        PersonPatchRequest request = buildPatch(null, null, null);

        assertThrows(IllegalArgumentException.class, () -> service.patch(1, request));
    }

    @Test
    void patch_admissionBeforeBirth_throwsIllegalArgumentException() {
        when(repository.findById(1)).thenReturn(Optional.of(jose));

        // Set admission before existing birth date (2000-04-06)
        PersonPatchRequest request = buildPatch(null, null, LocalDate.of(1999, 1, 1));

        assertThrows(IllegalArgumentException.class, () -> service.patch(1, request));
    }

    @Test
    void patch_notFound_throwsPersonNotFoundException() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        PersonPatchRequest request = buildPatch("X", null, null);

        assertThrows(PersonNotFoundException.class, () -> service.patch(99, request));
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    void delete_found_removesRecord() {
        when(repository.existsById(1)).thenReturn(true);

        service.delete(1);

        verify(repository).deleteById(1);
    }

    @Test
    void delete_notFound_throwsPersonNotFoundException() {
        when(repository.existsById(99)).thenReturn(false);

        assertThrows(PersonNotFoundException.class, () -> service.delete(99));
    }

    // ── calculateAge ─────────────────────────────────────────────────────────

    @Test
    void calculateAge_delegatesToCalculatorWithCurrentDate() {
        when(repository.findById(1)).thenReturn(Optional.of(jose));

        long expectedDays = ChronoUnit.DAYS.between(jose.getBirthDate(), LocalDate.now());
        when(ageCalculator.calculate(eq(jose.getBirthDate()), any(LocalDate.class), eq("days")))
                .thenReturn(expectedDays);

        long result = service.calculateAge(1, "days");

        assertEquals(expectedDays, result);
        verify(ageCalculator).calculate(eq(jose.getBirthDate()), any(LocalDate.class), eq("days"));
    }

    @Test
    void calculateAge_notFound_throwsPersonNotFoundException() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThrows(PersonNotFoundException.class, () -> service.calculateAge(99, "years"));
    }

    @Test
    void calculateAge_missingBirthDate_throwsInvalidPersonDataException() {
        Person invalid = new Person(1, "José da Silva", null, LocalDate.of(2020, 5, 10));
        when(repository.findById(1)).thenReturn(Optional.of(invalid));

        assertThrows(InvalidPersonDataException.class, () -> service.calculateAge(1, "days"));
        verify(ageCalculator, never()).calculate(any(), any(), anyString());
    }

    // ── calculateSalary ──────────────────────────────────────────────────────

    @Test
    void calculateSalary_delegatesToCalculatorWithCurrentDate() {
        when(repository.findById(1)).thenReturn(Optional.of(jose));

        BigDecimal expected = new BigDecimal("3259.36");
        when(salaryCalculator.calculate(eq(jose.getAdmissionDate()), any(LocalDate.class), eq("full")))
                .thenReturn(expected);

        BigDecimal result = service.calculateSalary(1, "full");

        assertEquals(expected, result);
        verify(salaryCalculator).calculate(eq(jose.getAdmissionDate()), any(LocalDate.class), eq("full"));
    }

    @Test
    void calculateSalary_notFound_throwsPersonNotFoundException() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThrows(PersonNotFoundException.class, () -> service.calculateSalary(99, "full"));
    }

    @Test
    void calculateSalary_missingAdmissionDate_throwsInvalidPersonDataException() {
        Person invalid = new Person(1, "José da Silva", LocalDate.of(2000, 4, 6), null);
        when(repository.findById(1)).thenReturn(Optional.of(invalid));

        assertThrows(InvalidPersonDataException.class, () -> service.calculateSalary(1, "full"));
        verify(salaryCalculator, never()).calculate(any(), any(), anyString());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private PersonRequest buildRequest(Integer id, String name,
                                       LocalDate birthDate, LocalDate admissionDate) {
        try {
            var request = new PersonRequest();
            setField(request, "id",            id);
            setField(request, "name",          name);
            setField(request, "birthDate",     birthDate);
            setField(request, "admissionDate", admissionDate);
            return request;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PersonPatchRequest buildPatch(String name, LocalDate birthDate, LocalDate admissionDate) {
        try {
            var request = new PersonPatchRequest();
            setField(request, "name",          name);
            setField(request, "birthDate",     birthDate);
            setField(request, "admissionDate", admissionDate);
            return request;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
