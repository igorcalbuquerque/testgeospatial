package com.sccon.geospatial.repository;

import com.sccon.geospatial.model.Person;

import java.util.List;
import java.util.Optional;

public interface PersonRepository {

    List<Person> findAll();

    Optional<Person> findById(Integer id);

    Person save(Person person);

    void deleteById(Integer id);

    boolean existsById(Integer id);
}
