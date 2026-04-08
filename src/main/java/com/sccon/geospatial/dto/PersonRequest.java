package com.sccon.geospatial.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public class PersonRequest {

    @Schema(description = "Person ID. If not provided, it will be auto-generated.", example = "4")
    private Integer id;

    @NotBlank(message = "Name is required")
    @Schema(description = "Full name", example = "Maria Oliveira")
    private String name;

    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    @JsonFormat(pattern = "dd/MM/yyyy")
    @Schema(description = "Birth date (dd/MM/yyyy)", example = "15/08/1995")
    private LocalDate birthDate;

    @NotNull(message = "Admission date is required")
    @PastOrPresent(message = "Admission date cannot be in the future")
    @JsonFormat(pattern = "dd/MM/yyyy")
    @Schema(description = "Admission date (dd/MM/yyyy)", example = "01/03/2019")
    private LocalDate admissionDate;

    public Integer getId() { return id; }
    public String getName() { return name; }
    public LocalDate getBirthDate() { return birthDate; }
    public LocalDate getAdmissionDate() { return admissionDate; }
}
