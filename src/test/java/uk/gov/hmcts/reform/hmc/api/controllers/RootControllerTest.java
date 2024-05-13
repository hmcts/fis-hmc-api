package uk.gov.hmcts.reform.hmc.api.controllers;

import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class RootControllerTest {

    @InjectMocks private RootController rootController;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

    }

    @Test
    void welcomeTest(){
        ResponseEntity<String> response = rootController.welcome();
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
