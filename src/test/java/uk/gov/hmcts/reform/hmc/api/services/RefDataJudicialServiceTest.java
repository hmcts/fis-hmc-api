package uk.gov.hmcts.reform.hmc.api.services;

import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.config.IdamTokenGenerator;
import uk.gov.hmcts.reform.hmc.api.exceptions.RefDataException;
import uk.gov.hmcts.reform.hmc.api.model.response.JudgeDetail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class RefDataJudicialServiceTest {

    @InjectMocks private RefDataJudicialServiceImpl refDataJudicialService;

    @Mock private RefDataJudicialApi refDataJudicialApi;

    @Mock private AuthTokenGenerator authTokenGenerator;

    @Mock private IdamTokenGenerator idamTokenGenerator;

    @Test
    public void shouldFetchJudgeDetailsRefDataJudicialTest() throws IOException, ParseException {
        JudgeDetail judgeDetail = JudgeDetail.judgeDetailWith().hearingJudgeName("test").build();
        List<JudgeDetail> judgeDetailsList = new ArrayList<>();
        judgeDetailsList.add(judgeDetail);
        when(authTokenGenerator.generate()).thenReturn("MOCK_S2S_TOKEN");
        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_AUTH_TOKEN");
        when(refDataJudicialApi.getJudgeDetails(anyString(), any(), any()))
                .thenReturn(judgeDetailsList);

        String judgeId = "4925644";
        JudgeDetail judgeDetailResp = refDataJudicialService.getJudgeDetails(judgeId);

        assertEquals("test", judgeDetailResp.getHearingJudgeName());
    }

    @Test
    public void shouldFetchJudgeDetailsRefDataJudicialS2sExceptionTest()
            throws IOException, ParseException {

        JudgeDetail judgeDetail = JudgeDetail.judgeDetailWith().hearingJudgeName("test").build();
        List<JudgeDetail> judgeDetailsList = new ArrayList<>();
        judgeDetailsList.add(judgeDetail);
        when(authTokenGenerator.generate())
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));
        when(idamTokenGenerator.generateIdamTokenForRefData()).thenReturn("MOCK_AUTH_TOKEN");
        when(refDataJudicialApi.getJudgeDetails(anyString(), any(), any()))
                .thenReturn(judgeDetailsList);

        String judgeId = "4925644";
        assertThrows(RefDataException.class, () -> refDataJudicialService.getJudgeDetails(judgeId));
    }
}
