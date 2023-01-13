package uk.gov.hmcts.reform.hmc.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;

@Service
public class ElasticSearchImpl implements ElasticSearch {

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    /**
     *  This method will call CCD searchCases API
     *  and return the result.
     *
     * @param authorisation Authorisation header
     * @param searchString json input for search
     * @param serviceAuthorisation S2S token
     * @param  caseType e.g. PRLAPPS
     * @return SearchResult object.
     */
    @Override
    public SearchResult searchCases(String authorisation, String searchString, String serviceAuthorisation, String caseType) {

        return coreCaseDataApi.searchCases(authorisation, serviceAuthorisation, caseType,
                                           searchString);

    }
}
