package uk.gov.hmcts.reform.hmc.api.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;

@RunWith(MockitoJUnitRunner.class)
public class ElasticSearchTest {

    @Mock private CoreCaseDataApi coreCaseDataApi;

    @InjectMocks private ElasticSearchImpl elasticSearch;

    public static final String TEST_AUTHORIZATION = "testAuthorisation";
    public static final String TEST_SERVICE_AUTHORIZATION = "testServiceAuthorisation";
    public static final String PRL_CASE_TYPE = "PRLAPPS";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("test case for CCD elastic search.")
    public void testSearchCases() throws IOException {

        String searchString = "";

        SearchResult mockResult =
                SearchResult.builder()
                        .cases(
                                Arrays.asList(
                                        CaseDetails.builder().caseTypeId(PRL_CASE_TYPE).build()))
                        .build();

        when(coreCaseDataApi.searchCases(
                        TEST_AUTHORIZATION,
                        TEST_SERVICE_AUTHORIZATION,
                        PRL_CASE_TYPE,
                        searchString))
                .thenReturn(mockResult);
        SearchResult searchResult =
                elasticSearch.searchCases(
                        TEST_AUTHORIZATION,
                        searchString,
                        TEST_SERVICE_AUTHORIZATION,
                        PRL_CASE_TYPE);
        assertNotNull(searchResult);
        assertEquals(PRL_CASE_TYPE, mockResult.getCases().get(0).getCaseTypeId());
    }
}
