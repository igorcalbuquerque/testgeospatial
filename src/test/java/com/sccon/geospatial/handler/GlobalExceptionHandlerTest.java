package com.sccon.geospatial.handler;

import com.sccon.geospatial.controller.PersonController;
import com.sccon.geospatial.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PersonService service = mock(PersonService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PersonController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void post_invalidDateFormat_returnsDetailedBadRequest() throws Exception {
        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Oliveira",
                                  "birthDate": "2000-04-06",
                                  "admissionDate": "10/05/2020"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed JSON request body"))
                .andExpect(jsonPath("$.violations[0].field").value("birthDate"));
    }

    @Test
    void patch_blankName_returnsDetailedBadRequest() throws Exception {
        mockMvc.perform(patch("/person/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.violations[0].field").value("name"));
    }
}
