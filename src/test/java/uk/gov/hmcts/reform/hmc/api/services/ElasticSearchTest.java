package uk.gov.hmcts.reform.hmc.api.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ElasticSearchTest {

    @Mock private CoreCaseDataApi coreCaseDataApi;

    @InjectMocks private ElasticSearchImpl elasticSearch;

    public static final String TEST_AUTHORIZATION = "testAuthorisation";
    public static final String TEST_SERVICE_AUTHORIZATION = "testServiceAuthorisation";
    public static final String PRL_CASE_TYPE = "PRLAPPS";

    @Test
    @DisplayName("test case for CCD elastic search.")
    void testSearchCases() {

        String searchString = "";

        SearchResult mockResult =
                SearchResult.builder()
                        .cases(List.of(CaseDetails.builder().caseTypeId(PRL_CASE_TYPE).build()))
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
        assertThat(searchResult.getCases())
            .singleElement()
            .extracting(CaseDetails::getCaseTypeId)
            .isEqualTo(PRL_CASE_TYPE);
    }
}
