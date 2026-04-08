package com.sccon.geospatial.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public class PersonPatchRequest {

    @Schema(description = "Full name", example = "Maria Oliveira")
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @JsonFormat(pattern = "dd/MM/yyyy")
    @Schema(description = "Birth date (dd/MM/yyyy)", example = "15/08/1995")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @JsonFormat(pattern = "dd/MM/yyyy")
    @Schema(description = "Admission date (dd/MM/yyyy)", example = "01/03/2019")
    @PastOrPresent(message = "Admission date cannot be in the future")
    private LocalDate admissionDate;

    public String getName() { return name; }
    public LocalDate getBirthDate() { return birthDate; }
    public LocalDate getAdmissionDate() { return admissionDate; }

    public boolean isEmpty() {
        return name == null && birthDate == null && admissionDate == null;
    }
}
