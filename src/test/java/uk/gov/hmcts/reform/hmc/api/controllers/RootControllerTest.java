package uk.gov.hmcts.reform.hmc.api.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
class RootControllerTest {

    @Test
    void welcomeTestShouldReturnWelcomeMessage() {
        RootController rootController = new RootController();
        ResponseEntity<String> response = rootController.welcome();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Welcome to fis-hmc-api dated Nov 01 2022", response.getBody());
    }

}
