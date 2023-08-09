package uk.gov.hmcts.reform.hmc.api.refdatavenue;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.hmc.api.clients.RefDataVenueApi;
import uk.gov.hmcts.reform.hmc.api.idam.IdamApiConsumerApplication;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;


@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {RefDataVenueApiConsumerApplication.class, IdamApiConsumerApplication.class})
@SuppressWarnings("unchecked")
public class RefDataVenueConsumerTestBase {

    @Autowired
    RefDataVenueApi refDataVenueApi;

    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";

    protected DslPart buildCourtsResponseDsl() {
        DslPart bodyPart =
                newJsonBody(
                                ob ->
                                        ob.array(
                                                "result",
                                                pa ->
                                                        pa.object(
                                                                u -> {
                                                                    u.stringType(
                                                                                    "courtVenueId",
                                                                                    "10047")
                                                                            .stringType(
                                                                                    "epimmsId",
                                                                                    "231596");
                                                                })))
                        .build();
        return bodyPart;
    }
}
