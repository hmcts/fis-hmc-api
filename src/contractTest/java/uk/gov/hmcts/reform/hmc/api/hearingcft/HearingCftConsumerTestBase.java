package uk.gov.hmcts.reform.hmc.api.hearingcft;

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
import uk.gov.hmcts.reform.hmc.api.services.HearingApiClient;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {HearingCftApiConsumerApplication.class, IdamApiConsumerApplication.class})
@SuppressWarnings("unchecked")
public class HearingCftConsumerTestBase {

    @Autowired
    HearingApiClient hearingApiClient;

    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";

    protected DslPart buildHearingsResponseDsl() {
        DslPart bodyPart =
                newJsonBody(
                                (body) -> {
                                    body.stringType("caseRef", "1669218448446672")
                                            .array(
                                                    "caseHearings",
                                                    (hearings) -> {
                                                        hearings.object(
                                                                (eachCase) -> {
                                                                    eachCase.stringType(
                                                                                    "hearingRequestDateTime",
                                                                                    "2022-12-15T15:55:18.744772")
                                                                            .stringType(
                                                                                    "hearingType",
                                                                                    "ABA5-APL")
                                                                            .stringType(
                                                                                    "hmcStatus",
                                                                                    "someHMCStatus")
                                                                            .stringType(
                                                                                    "lastResponseReceivedDateTime",
                                                                                    "2022-12-15T16:41:00")
                                                                            .numberType(
                                                                                    "requestVersion",
                                                                                    3)
                                                                            .stringType(
                                                                                    "hearingListingStatus",
                                                                                    "FIXED")
                                                                            .stringType(
                                                                                    "listAssistCaseStatus",
                                                                                    "LISTED")
                                                                            .minArrayLike(
                                                                                    "hearingDaySchedule",
                                                                                    1,
                                                                                    1,
                                                                                    sh -> {
                                                                                        sh.stringType(
                                                                                                        "hearingStartDateTime",
                                                                                                        "2022-12-16T10:00:00")
                                                                                                .stringType(
                                                                                                        "hearingEndDateTime",
                                                                                                        "2022-12-16T12:00:00")
                                                                                                .stringType(
                                                                                                        "hearingVenueId",
                                                                                                        "20262")
                                                                                                .stringType(
                                                                                                        "hearingJudgeId",
                                                                                                        "4925644");
                                                                                    })
                                                                            .stringType(
                                                                                    "hearingGroupRequestId",
                                                                                    null)
                                                                            .booleanType(
                                                                                    "hearingIsLinkedFlag",
                                                                                    true)
                                                                            .numberType(
                                                                                    "hearingID",
                                                                                    2000004424);
                                                                });
                                                    });
                                })
                        .build();
        return bodyPart;
    }
}
