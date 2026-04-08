package com.sccon.geospatial.service;

import com.sccon.geospatial.dto.PersonPatchRequest;
import com.sccon.geospatial.dto.PersonRequest;
import com.sccon.geospatial.dto.PersonResponse;

import java.math.BigDecimal;
import java.util.List;

public interface PersonService {

    List<PersonResponse> findAll();

    PersonResponse findById(Integer id);

    PersonResponse create(PersonRequest request);

    PersonResponse update(Integer id, PersonRequest request);

    PersonResponse patch(Integer id, PersonPatchRequest request);

    void delete(Integer id);

    long calculateAge(Integer id, String output);

    BigDecimal calculateSalary(Integer id, String output);
}
