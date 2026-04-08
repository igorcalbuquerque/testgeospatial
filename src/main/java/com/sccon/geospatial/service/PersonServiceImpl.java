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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class PersonServiceImpl implements PersonService {

    private final PersonRepository repository;
    private final AgeCalculator ageCalculator;
    private final SalaryCalculator salaryCalculator;

    public PersonServiceImpl(PersonRepository repository,
                             AgeCalculator ageCalculator,
                             SalaryCalculator salaryCalculator) {
        this.repository = repository;
        this.ageCalculator = ageCalculator;
        this.salaryCalculator = salaryCalculator;
    }

    @Override
    public List<PersonResponse> findAll() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Person::getName))
                .map(PersonResponse::from)
                .toList();
    }

    @Override
    public PersonResponse findById(Integer id) {
        return repository.findById(id)
                .map(PersonResponse::from)
                .orElseThrow(() -> new PersonNotFoundException(id));
    }

    @Override
    public PersonResponse create(PersonRequest request) {
        if (request.getId() != null && repository.existsById(request.getId())) {
            throw new PersonConflictException(request.getId());
        }
        validateRequiredDates(request.getBirthDate(), request.getAdmissionDate());
        validateDateConsistency(request.getBirthDate(), request.getAdmissionDate());

        Person person = new Person(
                request.getId(),
                request.getName(),
                request.getBirthDate(),
                request.getAdmissionDate()
        );
        return PersonResponse.from(repository.save(person));
    }

    @Override
    public PersonResponse update(Integer id, PersonRequest request) {
        if (!repository.existsById(id)) {
            throw new PersonNotFoundException(id);
        }
        validateRequiredDates(request.getBirthDate(), request.getAdmissionDate());
        validateDateConsistency(request.getBirthDate(), request.getAdmissionDate());

        Person person = new Person(id, request.getName(), request.getBirthDate(), request.getAdmissionDate());
        return PersonResponse.from(repository.save(person));
    }

    @Override
    public PersonResponse patch(Integer id, PersonPatchRequest request) {
        if (request.isEmpty()) {
            throw new IllegalArgumentException("At least one field must be provided for patch");
        }

        Person person = repository.findById(id)
                .orElseThrow(() -> new PersonNotFoundException(id));

        validatePatchFields(request, person);

        if (request.getName() != null)          person.setName(request.getName());
        if (request.getBirthDate() != null)     person.setBirthDate(request.getBirthDate());
        if (request.getAdmissionDate() != null) person.setAdmissionDate(request.getAdmissionDate());

        return PersonResponse.from(repository.save(person));
    }

    @Override
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new PersonNotFoundException(id);
        }
        repository.deleteById(id);
    }

    @Override
    public long calculateAge(Integer id, String output) {
        Person person = repository.findById(id)
                .orElseThrow(() -> new PersonNotFoundException(id));
        if (person.getBirthDate() == null) {
            throw new InvalidPersonDataException("Person with id " + id + " does not have a birth date");
        }
        return ageCalculator.calculate(person.getBirthDate(), LocalDate.now(), output);
    }

    @Override
    public BigDecimal calculateSalary(Integer id, String output) {
        Person person = repository.findById(id)
                .orElseThrow(() -> new PersonNotFoundException(id));
        if (person.getAdmissionDate() == null) {
            throw new InvalidPersonDataException("Person with id " + id + " does not have an admission date");
        }
        return salaryCalculator.calculate(person.getAdmissionDate(), LocalDate.now(), output);
    }

    private void validateRequiredDates(LocalDate birthDate, LocalDate admissionDate) {
        if (birthDate == null) {
            throw new InvalidPersonDataException("Birth date is required");
        }

        if (admissionDate == null) {
            throw new InvalidPersonDataException("Admission date is required");
        }
    }

    private void validateDateConsistency(LocalDate birthDate, LocalDate admissionDate) {
        if (admissionDate.isBefore(birthDate)) {
            throw new IllegalArgumentException(
                    "Admission date (" + admissionDate + ") cannot be before birth date (" + birthDate + ")");
        }
    }

    private void validatePatchFields(PersonPatchRequest request, Person current) {
        if (request.getName() != null && request.getName().isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }

        LocalDate today = LocalDate.now();

        if (request.getBirthDate() != null && !request.getBirthDate().isBefore(today)) {
            throw new IllegalArgumentException(
                    "Birth date (" + request.getBirthDate() + ") must be in the past");
        }

        if (request.getAdmissionDate() != null && request.getAdmissionDate().isAfter(today)) {
            throw new IllegalArgumentException(
                    "Admission date (" + request.getAdmissionDate() + ") cannot be in the future");
        }

        LocalDate effectiveBirthDate      = request.getBirthDate()     != null ? request.getBirthDate()     : current.getBirthDate();
        LocalDate effectiveAdmissionDate  = request.getAdmissionDate() != null ? request.getAdmissionDate() : current.getAdmissionDate();

        validateRequiredDates(effectiveBirthDate, effectiveAdmissionDate);

        if (effectiveAdmissionDate.isBefore(effectiveBirthDate)) {
            throw new IllegalArgumentException(
                    "Admission date (" + effectiveAdmissionDate + ") cannot be before birth date (" + effectiveBirthDate + ")");
        }
    }
}
