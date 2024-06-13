package uk.gov.hmcts.reform.hmc.api.hearingcft;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.hmc.api.model.response.Hearings;

@PactTestFor(providerName = "hearingData_cftInternal", port = "8894")
@TestPropertySource(
        properties = {"hearing_component.api.feign-url=localhost:8894", "idam.api.url=localhost:5000"})
public class HearingCftInternalConsumerTest extends HearingCftConsumerTestBase {

    @Pact(provider = "hearingData_cftInternal", consumer = "fpl_hearingDataCftConfiguration")
    public RequestResponsePact generatePactFragmentForGetHearingsByCaseRefNo(
            PactDslWithProvider builder) throws Exception {
        // @formatter:off
        return builder.given("Hearings exists for given caseRefNo")
                .uponReceiving("A Request to get hearings by caseRefNo")
                .method("GET")
                .headers(
                        SERVICE_AUTHORIZATION_HEADER,
                        SERVICE_AUTH_TOKEN,
                        AUTHORIZATION_HEADER,
                        AUTHORIZATION_TOKEN)
                .path("/hearings/caseReference")
                .willRespondWith()
                .body(buildHearingsResponseDsl())
                .status(HttpStatus.SC_OK)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForGetHearingsByCaseRefNo")
    public void verifyGetHearingsByCaseRefNo() {
        Hearings hearings =
            hearingApiClient.getHearingDetails(AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN, "caseReference");

        assertThat(hearings, is(notNullValue()));
        assertThat(hearings.getCaseHearings().get(0).getHmcStatus(), is("someHMCStatus"));
    }
}
