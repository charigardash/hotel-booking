package com.hotelbooking.property.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Walks the full first flow: POST owner -> POST property -> GET search.
 * Exercises controller + service + repository + domain + Jackson + exception handler.
 * If any of those are broken, this test fails.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PropertyFlowIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void ownerOnboardsPropertyAndPropertyAppearsInSearch() throws Exception {
        // 1. Create an owner.
        String ownerJson = mockMvc.perform(post("/api/v1/owners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Chain Co","email":"chain@example.com"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String ownerId = objectMapper.readTree(ownerJson).get("id").asText();

        // 2. Onboard a property under that owner.
        String propertyRequestBody = """
                {
                  "ownerId":"%s","name":"Sea View",
                  "addressLine":"1 Beach Rd","locality":"Bandra",
                  "city":"Mumbai","state":"MH","pincode":"400050",
                  "starRating":4,"amenities":["WIFI","POOL"],
                  "roomTypes":[{"name":"Deluxe","maxGuests":3,"basePrice":2000,"totalUnits":2}]
                }
                """.formatted(ownerId);

        String propertyJson = mockMvc.perform(post("/api/v1/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propertyRequestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sea View"))
                .andExpect(jsonPath("$.city").value("Mumbai"))
                .andExpect(jsonPath("$.roomTypes[0].name").value("Deluxe"))
                .andReturn().getResponse().getContentAsString();

        String propertyId = objectMapper.readTree(propertyJson).get("id").asText();

        // 3. Search finds it.
        mockMvc.perform(get("/api/v1/properties")
                        .param("city", "Mumbai")
                        .param("guests", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(propertyId))
                .andExpect(jsonPath("$[0].name").value("Sea View"));

        // 4. Owner listing finds it too.
        mockMvc.perform(get("/api/v1/owners/{ownerId}/properties", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(propertyId));
    }

    @Test
    void unknownOwnerOnPropertyPostReturns404() throws Exception {
        mockMvc.perform(post("/api/v1/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ownerId":"does-not-exist","name":"X",
                                 "addressLine":"1","locality":"L","city":"C","state":"S","pincode":"1",
                                 "starRating":4,"amenities":[],
                                 "roomTypes":[{"name":"Deluxe","maxGuests":2,"basePrice":2000,"totalUnits":1}]}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidBookingDatesOnSearchReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/properties")
                        .param("checkIn", "2026-01-10"))  // checkOut missing
                .andExpect(status().isBadRequest());
    }
}
