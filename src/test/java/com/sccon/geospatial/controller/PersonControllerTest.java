package com.sccon.geospatial.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sccon.geospatial.dto.PersonRequest;
import com.sccon.geospatial.dto.PersonResponse;
import com.sccon.geospatial.exception.PersonConflictException;
import com.sccon.geospatial.exception.PersonNotFoundException;
import com.sccon.geospatial.handler.GlobalExceptionHandler;
import com.sccon.geospatial.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PersonControllerTest {

    @Mock  private PersonService service;
    @InjectMocks private PersonController controller;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    private PersonResponse joseResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        joseResponse = buildResponse(1, "José da Silva",
                LocalDate.of(2000, 4, 6), LocalDate.of(2020, 5, 10));
    }

    // ── GET /person ──────────────────────────────────────────────────────────

    @Test
    void findAll_returns200WithList() throws Exception {
        when(service.findAll()).thenReturn(List.of(joseResponse));

        mockMvc.perform(get("/person"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("José da Silva"));
    }

    // ── GET /person/{id} ─────────────────────────────────────────────────────

    @Test
    void findById_found_returns200() throws Exception {
        when(service.findById(1)).thenReturn(joseResponse);

        mockMvc.perform(get("/person/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void findById_notFound_returns404() throws Exception {
        when(service.findById(99)).thenThrow(new PersonNotFoundException(99));

        mockMvc.perform(get("/person/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Person with id 99 not found"));
    }

    // ── POST /person ─────────────────────────────────────────────────────────

    @Test
    void create_validRequest_returns201() throws Exception {
        when(service.create(any())).thenReturn(joseResponse);

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"José da Silva\",\"birthDate\":\"06/04/2000\",\"admissionDate\":\"10/05/2020\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_missingName_returns400WithViolations() throws Exception {
        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"birthDate\":\"06/04/2000\",\"admissionDate\":\"10/05/2020\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations[0].field").value("name"));
    }

    @Test
    void create_missingDates_returns400WithViolations() throws Exception {
        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"José da Silva\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations[?(@.field == 'birthDate')]").exists())
                .andExpect(jsonPath("$.violations[?(@.field == 'admissionDate')]").exists());
    }

    @Test
    void create_conflictId_returns409() throws Exception {
        when(service.create(any())).thenThrow(new PersonConflictException(1));

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"José da Silva\",\"birthDate\":\"06/04/2000\",\"admissionDate\":\"10/05/2020\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // ── DELETE /person/{id} ──────────────────────────────────────────────────

    @Test
    void delete_found_returns204() throws Exception {
        doNothing().when(service).delete(1);

        mockMvc.perform(delete("/person/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new PersonNotFoundException(99)).when(service).delete(99);

        mockMvc.perform(delete("/person/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /person/{id}/age ─────────────────────────────────────────────────

    @Test
    void getAge_days_returns200WithDynamicValue() throws Exception {
        LocalDate birthDate = LocalDate.of(2000, 4, 6);
        long expectedDays = ChronoUnit.DAYS.between(birthDate, LocalDate.now());

        when(service.calculateAge(1, "days")).thenReturn(expectedDays);

        mockMvc.perform(get("/person/1/age").param("output", "days"))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedDays)));
    }

    @Test
    void getAge_invalidOutput_returns400() throws Exception {
        when(service.calculateAge(eq(1), eq("weeks")))
                .thenThrow(new com.sccon.geospatial.exception.InvalidOutputFormatException("weeks", "days, months, years"));

        mockMvc.perform(get("/person/1/age").param("output", "weeks"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ── GET /person/{id}/salary ──────────────────────────────────────────────

    @Test
    void getSalary_full_returns200() throws Exception {
        when(service.calculateSalary(1, "full")).thenReturn(new BigDecimal("3259.36"));

        mockMvc.perform(get("/person/1/salary").param("output", "full"))
                .andExpect(status().isOk())
                .andExpect(content().string("3259.36"));
    }

    @Test
    void getSalary_notFound_returns404() throws Exception {
        when(service.calculateSalary(eq(99), anyString()))
                .thenThrow(new PersonNotFoundException(99));

        mockMvc.perform(get("/person/99/salary").param("output", "full"))
                .andExpect(status().isNotFound());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private PersonResponse buildResponse(Integer id, String name,
                                          LocalDate birthDate, LocalDate admissionDate) {
        var person = new com.sccon.geospatial.model.Person(id, name, birthDate, admissionDate);
        return PersonResponse.from(person);
    }
}
