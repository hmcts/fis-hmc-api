package uk.gov.hmcts.reform.hmc.api.refdatajudge;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.hmc.api.idam.IdamApiConsumerApplication;
import uk.gov.hmcts.reform.hmc.api.services.RefDataJudicialApi;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {RefDataJudgeApiConsumerApplication.class, IdamApiConsumerApplication.class})
@SuppressWarnings("unchecked")
public class RefDataJudgeConsumerTestBase {

    @Autowired RefDataJudicialApi refDataJudgeApi;

    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";

    protected DslPart buildJudicialResponseDsl() {
        DslPart bodyPart =
                newJsonBody(
                                ob ->
                                        ob.array(
                                                "users",
                                                pa ->
                                                        pa.object(
                                                                u -> {
                                                                    u.stringType(
                                                                                    "hearingJudgeName",
                                                                                    "Henry Taylor")
                                                                            .stringType(
                                                                                    "personalCode",
                                                                                    "4925644");
                                                                })))
                        .build();
        return bodyPart;
    }
}
