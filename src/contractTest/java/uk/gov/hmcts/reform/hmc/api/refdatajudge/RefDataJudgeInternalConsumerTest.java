package uk.gov.hmcts.reform.hmc.api.refdatajudge;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.hmc.api.model.request.JudgeRequestDTO;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@PactTestFor(providerName = "referenceData_judgeInternal", port = "8894")
@TestPropertySource(
        properties = {"ref_data_judicial.api.url=localhost:8894", "idam.api.url=localhost:5000"})
public class RefDataJudgeInternalConsumerTest extends RefDataJudgeConsumerTestBase {

    @Pact(provider = "referenceData_judgeInternal", consumer = "fpl_judgeConfiguration")
    public RequestResponsePact generatePactFragmentForGetJudgeDetailsId(PactDslWithProvider builder)
            throws JsonProcessingException {
        // @formatter:off
        return builder.given("Judge exists for given personal_code or Id ")
                .uponReceiving("A Request to get judge details by id")
                .method("POST")
                .headers(
                        SERVICE_AUTHORIZATION_HEADER,
                        SERVICE_AUTH_TOKEN,
                        AUTHORIZATION_HEADER,
                        AUTHORIZATION_TOKEN)
                .path("/refdata/judicial/users")
                .body(
                        new ObjectMapper().writeValueAsString(buildJudgeRequestContent()),
                        "application/json")
                .willRespondWith()
                .body(buildJudicialResponseDsl())
                .status(HttpStatus.SC_OK)
                .toPact();
    }

    private JudgeRequestDTO buildJudgeRequestContent() {
        List<String> personalCodeList = new ArrayList<>();
        personalCodeList.add("judgeId");
        return JudgeRequestDTO.judgeRequestWith().personalCode(personalCodeList).build();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForGetJudgeDetailsId")
    public void verifyGetJudgeDetailsById() throws JsonProcessingException {
        JSONObject judgeDetailList =
                refDataJudgeApi.getJudgeDetails(
                        AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN, buildJudgeRequestContent());
        assertThat(judgeDetailList, is(notNullValue()));
    }
}
