package uk.gov.hmcts.reform.hmc.api.services;

import uk.gov.hmcts.reform.ccd.client.model.SearchResult;

public interface ElasticSearch {

    SearchResult searchCases(
            String authorisation,
            String searchString,
            String serviceAuthorisation,
            String caseType);
}
