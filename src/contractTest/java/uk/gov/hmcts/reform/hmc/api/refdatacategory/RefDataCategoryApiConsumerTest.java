package uk.gov.hmcts.reform.hmc.api.refdatacategory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.hmc.api.idam.IdamApiConsumerApplication;
import uk.gov.hmcts.reform.hmc.api.model.response.Categories;
import uk.gov.hmcts.reform.hmc.api.restclient.HmcHearingApi;
import uk.gov.hmcts.reform.hmc.api.utils.ResourceLoader;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "referenceDataCategoryApi", port = "8894")
@TestPropertySource(properties = {"hearing.api.url=localhost:8894", "idam.api.url=localhost:5000"})
@ContextConfiguration(
        classes = {RefDataCategoryApiConsumerApplication.class, IdamApiConsumerApplication.class})
public class RefDataCategoryApiConsumerTest {

    private final String response = "response/RefDataCategoryResponse.json";

    @Autowired HmcHearingApi hmcHearingApi;

    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";

    @Pact(provider = "referenceDataCategoryApi", consumer = "fis_hmc_api")
    public RequestResponsePact generateCategoryApiConsumerTest(PactDslWithProvider builder)
            throws Exception {

        return builder.given("case hearing/hearings exist for a case ")
                .uponReceiving(
                        "A Request to get list of values for a given service id and category id")
                .method("GET")
                .headers(
                        SERVICE_AUTHORIZATION_HEADER,
                        SERVICE_AUTH_TOKEN,
                        AUTHORIZATION_HEADER,
                        AUTHORIZATION_TOKEN)
                .headers("Content-Type", "application/json")
                .path("/refdata/commondata/lov/categories/HearingType")
                .query("serviceId=ABA5")
                .willRespondWith()
                .body(ResourceLoader.loadJson(response), "application/json")
                .status(HttpStatus.SC_OK)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generateCategoryApiConsumerTest")
    public void verifyCateogryListOfValues() {

        final Categories categories =
                hmcHearingApi.retrieveListOfValuesByCategoryId(
                        AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN, "HearingType", "ABA5");

        assertNotNull(categories);
    }
}