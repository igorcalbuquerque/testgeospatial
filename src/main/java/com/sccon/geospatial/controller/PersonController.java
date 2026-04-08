package com.sccon.geospatial.controller;

import com.sccon.geospatial.dto.ErrorResponse;
import com.sccon.geospatial.dto.PersonPatchRequest;
import com.sccon.geospatial.dto.PersonRequest;
import com.sccon.geospatial.dto.PersonResponse;
import com.sccon.geospatial.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/person")
@Tag(name = "Person", description = "Person management API")
public class PersonController {

    private final PersonService service;

    public PersonController(PersonService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all people sorted alphabetically by name")
    public ResponseEntity<List<PersonResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a person by ID",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    public ResponseEntity<PersonResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new person",
            responses = {
                    @ApiResponse(responseCode = "201"),
                    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    public ResponseEntity<PersonResponse> create(@Valid @RequestBody PersonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a person's data entirely",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    public ResponseEntity<PersonResponse> update(@PathVariable Integer id,
                                                 @Valid @RequestBody PersonRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a person",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    public ResponseEntity<PersonResponse> patch(@PathVariable Integer id,
                                                @Valid @RequestBody PersonPatchRequest request) {
        return ResponseEntity.ok(service.patch(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a person",
            responses = {
                    @ApiResponse(responseCode = "204"),
                    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/age")
    @Operation(summary = "Get person's age in days, months or years",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    public ResponseEntity<Long> getAge(
            @PathVariable Integer id,
            @Parameter(description = "Output format", schema = @Schema(allowableValues = {"days", "months", "years"}))
            @RequestParam String output) {
        return ResponseEntity.ok(service.calculateAge(id, output));
    }

    @GetMapping("/{id}/salary")
    @Operation(summary = "Get person's salary in full (R$) or in minimum wages",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    public ResponseEntity<BigDecimal> getSalary(
            @PathVariable Integer id,
            @Parameter(description = "Output format", schema = @Schema(allowableValues = {"full", "min"}))
            @RequestParam String output) {
        return ResponseEntity.ok(service.calculateSalary(id, output));
    }
}
