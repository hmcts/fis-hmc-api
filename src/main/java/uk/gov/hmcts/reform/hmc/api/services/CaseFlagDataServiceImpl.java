package uk.gov.hmcts.reform.hmc.api.services;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.EMPTY_STRING;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.mapper.FisHmcObjectMapper;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseDetailResponse;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Element;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Flags;
import uk.gov.hmcts.reform.hmc.api.model.ccd.PartyDetails;
import uk.gov.hmcts.reform.hmc.api.model.ccd.flagdata.FlagDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseFlags;
import uk.gov.hmcts.reform.hmc.api.model.response.IndividualDetailsModel;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyDetailsModel;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyFlagsModel;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyType;
import uk.gov.hmcts.reform.hmc.api.model.response.ServiceHearingValues;
import uk.gov.hmcts.reform.hmc.api.utils.Constants;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseFlagDataServiceImpl {

    /**
     * deserialization ccd record for mapping the case flag data
     *
     * @param caseDetails
     * @return CaseDetailResponse deserialization object
     * @throws IOException
     */
    public CaseDetailResponse getCcdCaseData(CaseDetails caseDetails) throws IOException {
        ObjectMapper objectMapper = FisHmcObjectMapper.getObjectMapper();

        CaseDetailResponse ccdResponse =
                objectMapper.convertValue(caseDetails, CaseDetailResponse.class);

        return ccdResponse;
    }

    /**
     * mapping the all parties flag data to ServiceHearingValues .
     *
     * @param serviceHearingValues
     * @param caseDetails
     * @throws IOException
     */
    public void setCaseFlagData(ServiceHearingValues serviceHearingValues, CaseDetails caseDetails)
            throws IOException {

        List<PartyFlagsModel> partiesFlagslList = new ArrayList<>();
        List<PartyDetailsModel> partyDetailsModelList = new ArrayList<>();
        CaseDetailResponse ccdResponse = getCcdCaseData(caseDetails);
        List<Element<PartyDetails>> applicantLst = ccdResponse.getCaseData().getApplicants();

        addPartyFlagData(partiesFlagslList, partyDetailsModelList, applicantLst);

        List<Element<PartyDetails>> respondedLst = ccdResponse.getCaseData().getRespondents();

        addPartyFlagData(partiesFlagslList, partyDetailsModelList, respondedLst);

        CaseFlags caseFlags = CaseFlags.caseFlagsWith().flags(partiesFlagslList).build();

        serviceHearingValues.setCaseFlags(caseFlags);

        serviceHearingValues.setParties(partyDetailsModelList);
    }

    private void addPartyFlagData(
            List<PartyFlagsModel> partiesFlagslList,
            List<PartyDetailsModel> partyDetailsModelList,
            List<Element<PartyDetails>> applicantLst) {
        IndividualDetailsModel individualDetailsModel;
        String uuid;
        PartyDetails partyDetails;
        PartyDetailsModel partyDetailsModel;
        for (Element<PartyDetails> party : applicantLst) {
            uuid = party.getId().toString();
            partyDetails = party.getValue();
            partiesFlagslList.addAll(getPartyFlagsModel(partyDetails, uuid));

            individualDetailsModel =
                    IndividualDetailsModel.individualDetailsWith()
                            .firstName(partyDetails.getFirstName())
                            .lastName(partyDetails.getLastName())
                            .build();

            partyDetailsModel =
                    PartyDetailsModel.partyDetailsWith()
                            .partyID(uuid)
                            .partyName(
                                    partyDetails.getFirstName()
                                            + EMPTY_STRING
                                            + partyDetails.getLastName())
                            .partyType(PartyType.IND)
                            .partyRole(Constants.APPLICANT)
                            .individualDetails(individualDetailsModel)
                            .build();

            partyDetailsModelList.add(partyDetailsModel);
        }
    }

    private List<PartyFlagsModel> getPartyFlagsModel(PartyDetails partyDetails, String uuid) {
        PartyFlagsModel partyFlagsModel = null;
        List<PartyFlagsModel> partyFlagsModelList = new ArrayList<>();
        Flags flag = partyDetails.getPartyLevelFlag();
        List<Element<FlagDetail>> detailsLST = flag.getDetails();

        for (Element<FlagDetail> flagDetailElement : detailsLST) {
            FlagDetail flagDetail = flagDetailElement.getValue();

            partyFlagsModel =
                    PartyFlagsModel.partyFlagsModelWith()
                            .partyId(uuid)
                            .partyName(
                                    partyDetails.getFirstName()
                                            + EMPTY_STRING
                                            + partyDetails.getLastName())
                            .flagId(flagDetail.getData().getFlagCode())
                            .flagStatus(flagDetail.getData().getStatus())
                            .flagParentId("")
                            .flagDescription(flagDetail.getData().getFlagComment())
                            .build();
            partyFlagsModelList.add(partyFlagsModel);
        }

        return partyFlagsModelList;
    }
}
