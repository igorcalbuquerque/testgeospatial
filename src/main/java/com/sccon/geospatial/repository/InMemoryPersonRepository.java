package com.sccon.geospatial.repository;

import com.sccon.geospatial.model.Person;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class InMemoryPersonRepository implements PersonRepository {

    private final ConcurrentHashMap<Integer, Person> store = new ConcurrentHashMap<>();
    private final AtomicInteger idSequence = new AtomicInteger(0);

    public InMemoryPersonRepository() {
        save(new Person(null, "Ana Costa",      LocalDate.of(1990, 3, 15),  LocalDate.of(2018, 7,  1)));
        save(new Person(null, "Carlos Mendes",  LocalDate.of(1985, 11, 22), LocalDate.of(2015, 1, 10)));
        save(new Person(null, "José da Silva",  LocalDate.of(2000, 4,  6),  LocalDate.of(2020, 5, 10)));
    }

    @Override
    public List<Person> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Person> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Person save(Person person) {
        if (person.getId() == null) {
            person.setId(idSequence.incrementAndGet());
        } else {
            idSequence.accumulateAndGet(person.getId(), Math::max);
        }
        store.put(person.getId(), person);
        return person;
    }

    @Override
    public void deleteById(Integer id) {
        store.remove(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return store.containsKey(id);
    }
}
