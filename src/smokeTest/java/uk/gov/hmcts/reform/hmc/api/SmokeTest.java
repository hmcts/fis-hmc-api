package uk.gov.hmcts.reform.hmc.api;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

class SmokeTest {

    private final String targetInstance =
            StringUtils.defaultIfBlank(System.getenv("TEST_URL"), "http://localhost:4550");

    @Test
    void shouldCheckApplicationHealthUp() {

        RequestSpecification requestSpecification =
                new RequestSpecBuilder()
                        .setBaseUri(targetInstance)
                        .setRelaxedHTTPSValidation()
                        .build();

        Response response =
                given(requestSpecification).when().get("/health").then().extract().response();

        assertThat(response.getBody().asString()).contains("UP");
    }
}
