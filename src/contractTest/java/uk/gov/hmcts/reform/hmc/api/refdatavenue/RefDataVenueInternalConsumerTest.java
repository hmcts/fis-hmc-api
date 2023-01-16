package uk.gov.hmcts.reform.hmc.api.refdatavenue;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@PactTestFor(providerName = "referenceData_venueInternal", port = "8894")
@TestPropertySource(properties = {"rd_venue.api.url=localhost:8894", "idam.api.url=localhost:5000"})
public class RefDataVenueInternalConsumerTest extends RefDataVenueConsumerTestBase {

    @Pact(provider = "referenceData_venueInternal", consumer = "fpl_venueConfiguration")
    public RequestResponsePact generatePactFragmentForGetCourtDetailsByEpimmsId(
            PactDslWithProvider builder) {
        // @formatter:off
        return builder.given("Courts exists for given epimmsId")
                .uponReceiving("A Request to get court details by epimmsId")
                .method("GET")
                .headers(
                        SERVICE_AUTHORIZATION_HEADER,
                        SERVICE_AUTH_TOKEN,
                        AUTHORIZATION_HEADER,
                        AUTHORIZATION_TOKEN)
                .path("/refdata/location/court-venues")
                .query("epimms_id=epimms_id")
                .willRespondWith()
                .body(buildCourtsResponseDsl())
                .status(HttpStatus.SC_OK)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForGetCourtDetailsByEpimmsId")
    public void verifyGetCourtByEpimmsId() {
        JSONObject courtDetailList =
                refDataVenueApi.getCourtDetails(
                        AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN, "epimms_id");
        assertThat(courtDetailList, is(notNullValue()));
    }
}
