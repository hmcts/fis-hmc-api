package uk.gov.hmcts.reform.hmc.api.services;

import static uk.gov.hmcts.reform.hmc.api.utils.Constants.APPLICANT;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.EMPTY_STRING;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.RESPONDENT;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.mapper.FisHmcObjectMapper;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseDetailResponse;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseManagementLocation;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Element;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Flags;
import uk.gov.hmcts.reform.hmc.api.model.ccd.PartyDetails;
import uk.gov.hmcts.reform.hmc.api.model.ccd.flagdata.FlagDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseFlags;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingLocation;
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
        setBaseLocation(serviceHearingValues, ccdResponse);
        List<Element<PartyDetails>> applicantLst = ccdResponse.getCaseData().getApplicants();
        if (null != applicantLst) {
            addPartyFlagData(partiesFlagslList, partyDetailsModelList, applicantLst, APPLICANT);
        }

        List<Element<PartyDetails>> respondedLst = ccdResponse.getCaseData().getRespondents();
        if (null != respondedLst) {
            addPartyFlagData(partiesFlagslList, partyDetailsModelList, respondedLst, RESPONDENT);
        }

        PartyDetails applicantsFL401 = ccdResponse.getCaseData().getApplicantsFL401();
        if (null != applicantsFL401) {
            addFL401PartyFlagData(
                    partiesFlagslList, partyDetailsModelList, applicantsFL401, APPLICANT);
        }

        PartyDetails respondentsFL401 = ccdResponse.getCaseData().getRespondentsFL401();
        if (null != respondentsFL401) {
            addFL401PartyFlagData(
                    partiesFlagslList, partyDetailsModelList, respondentsFL401, RESPONDENT);
        }
        if (null != partiesFlagslList && null != partyDetailsModelList) {
            CaseFlags caseFlags = CaseFlags.caseFlagsWith().flags(partiesFlagslList).build();

            serviceHearingValues.setCaseFlags(caseFlags);

            serviceHearingValues.setParties(partyDetailsModelList);
        }
    }

    /**
     * Hearing Base Location id setting for service values fromm ccd record
     *
     * @param serviceHearingValues
     * @param ccdResponse
     */
    private void setBaseLocation(
            ServiceHearingValues serviceHearingValues, CaseDetailResponse ccdResponse) {
        CaseManagementLocation caseManagementLocation =
                ccdResponse.getCaseData().getCaseManagementLocation();

        if (null != caseManagementLocation && null != caseManagementLocation.getBaseLocation()) {
            serviceHearingValues.setCaseManagementLocationCode(
                    caseManagementLocation.getBaseLocation());
            List<HearingLocation> locationList =
                    Arrays.asList(
                            HearingLocation.hearingLocationWith()
                                    .locationType(Constants.EMPTY)
                                    .locationId(caseManagementLocation.getBaseLocation())
                                    .build());
            serviceHearingValues.setHearingLocations(locationList);
        }
    }

    private void addPartyFlagData(
            List<PartyFlagsModel> partiesFlagslList,
            List<PartyDetailsModel> partyDetailsModelList,
            List<Element<PartyDetails>> partyLst,
            String role) {
        IndividualDetailsModel individualDetailsModel;
        String uuid;
        PartyDetails partyDetails;
        PartyDetailsModel partyDetailsModel;
        for (Element<PartyDetails> party : partyLst) {
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
                            .partyRole(role)
                            .individualDetails(individualDetailsModel)
                            .build();

            partyDetailsModelList.add(partyDetailsModel);
        }
    }

    private void addFL401PartyFlagData(
            List<PartyFlagsModel> partiesFlagslList,
            List<PartyDetailsModel> partyDetailsModelList,
            PartyDetails partyDetails,
            String role) {
        IndividualDetailsModel individualDetailsModel;
        String uuid;

        PartyDetailsModel partyDetailsModel;
        if (null != partyDetails) {
            uuid = UUID.randomUUID().toString();

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
                            .partyRole(role)
                            .individualDetails(individualDetailsModel)
                            .build();

            partyDetailsModelList.add(partyDetailsModel);
        }
    }

    private List<PartyFlagsModel> getPartyFlagsModel(PartyDetails partyDetails, String uuid) {
        PartyFlagsModel partyFlagsModel = null;
        List<PartyFlagsModel> partyFlagsModelList = new ArrayList<>();
        Flags flag = partyDetails.getPartyLevelFlag();
        if (flag == null) {
            return partyFlagsModelList;
        }
        List<Element<FlagDetail>> detailsLST = flag.getDetails();

        for (Element<FlagDetail> flagDetailElement : detailsLST) {
            FlagDetail flagDetail = flagDetailElement.getValue();
            if (null != flagDetail) {
                partyFlagsModel =
                        PartyFlagsModel.partyFlagsModelWith()
                                .partyId(uuid)
                                .partyName(
                                        partyDetails.getFirstName()
                                                + EMPTY_STRING
                                                + partyDetails.getLastName())
                                .flagId(flagDetail.getFlagCode())
                                .flagStatus(flagDetail.getStatus())
                                .flagParentId("")
                                .flagDescription(flagDetail.getName())
                                .build();
                partyFlagsModelList.add(partyFlagsModel);
            }
        }

        return partyFlagsModelList;
    }
}
