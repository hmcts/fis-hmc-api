package uk.gov.hmcts.reform.hmc.api.services;

import uk.gov.hmcts.reform.ccd.client.model.SearchResult;

public interface ElasticSearch {

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
    public SearchResult searchCases(String authorisation, String searchString, String serviceAuthorisation, String caseType) ;
}


