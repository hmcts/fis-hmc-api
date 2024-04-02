package uk.gov.hmcts.reform.hmc.api.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseData;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseDetailResponse;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Element;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Flags;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Organisation;
import uk.gov.hmcts.reform.hmc.api.model.ccd.PartyDetails;
import uk.gov.hmcts.reform.hmc.api.model.ccd.flagdata.FlagDetail;
import uk.gov.hmcts.reform.hmc.api.model.request.CaseCategories;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingRequest;

import uk.gov.hmcts.reform.hmc.api.model.request.HearingDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.IndividualDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingLocation;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingWindow;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyFlagsModel;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyType;
import uk.gov.hmcts.reform.hmc.api.services.CaseFlagDataServiceImpl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.AND;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.APPLICANT;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.APPLICANT_CASE_NAME;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.C100;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_FILE_VIEW;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.COURT;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.EMPTY;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.EMPTY_STRING;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.FALSE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.FL401;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.FL401_APPLICANT_TABLE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.FL401_RESPONDENT_TABLE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.HEARING_PRIORITY;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.HEARING_TYPE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ISSUE_DATE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.LAST_NAME;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ONE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ORGANISATION;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.PF0002;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.PF0013;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.PF0015;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.PF0018;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.PLUS_SIGN;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.RA;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.RA0042;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.RESPONDENT;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.RE_MINOR;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.SM;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.SM0002;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.TRUE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.UNDERSCORE;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomatedHearingTransactionRequestMapper {
    @Value("${ccd.ui.url}")
    private String ccdBaseUrl;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CaseFlagDataServiceImpl caseFlagDataServiceImpl;

    public AutomatedHearingRequest mappingHearingTransactionRequest(CaseDetails caseDetails) throws IOException {
        AutomatedHearingRequest hearingRequest = AutomatedHearingRequest.automatedHearingRequestWith().build();
        hearingRequest.setCaseDetails(uk.gov.hmcts.reform.hmc.api.model.request.CaseDetails.automatedCaseDetailsWith()
                                          .hmctsServiceCode("ABA5") //Hardcoded in prl-cos-api
                                          .caseRef(caseDetails.getId().toString())
                                          .requestTimeStamp(LocalDateTime.now())
                                          .externalCaseReference("") //Need to verify
                                          .caseDeepLink(ccdBaseUrl + "caseReference" + CASE_FILE_VIEW) //Need to verify
                                          .hmctsInternalCaseName("")
                                          .publicCaseName("")
                                          .caseAdditionalSecurityFlag(Boolean.TRUE)
                                          .caseInterpreterRequiredFlag(Boolean.TRUE)
                                          .caseCategories(CaseCategories.CaseCategoriesWith().build())
                                          .caseManagementLocationCode("")
                                          .caseRestrictedFlag(Boolean.TRUE)
                                          .caseSlaStartDate("")
                                          .build());
      //  caseDetails.getData().get("ordershearingdetails").toString().
        CaseDetailResponse ccdResponse = caseFlagDataServiceImpl.getCcdCaseData(caseDetails);
        List<uk.gov.hmcts.reform.hmc.api.model.request.PartyDetails> partyDetailsList = getPartyDetails(ccdResponse);
        hearingRequest.setPartyDetails(partyDetailsList);

        HearingDetails hearingDetails = getHearingDetails(caseDetails.getId().toString(), ccdResponse.getCaseData());
        hearingRequest.setHearingDetails(hearingDetails);

        return hearingRequest;

    }

    private HearingDetails getHearingDetails(String id, CaseData caseData) {
        String publicCaseNameMapper = EMPTY;
        Boolean privateHearingRequiredFlagMapper = FALSE;
        if (FL401.equals(caseData.getCaseTypeOfApplication())) {
            PartyDetails applicantMap = caseData.getApplicantsFL401();
            PartyDetails respondentTableMap = caseData.getRespondentsFL401();
            publicCaseNameMapper = (applicantMap != null && respondentTableMap != null)
                    ? applicantMap.getLastName() + AND + respondentTableMap.getLastName() : EMPTY;
        } else if (C100.equals(caseData.getCaseTypeOfApplication())) {
            publicCaseNameMapper = RE_MINOR;
            privateHearingRequiredFlagMapper = TRUE;
        }
        String hmctsInternalCaseNameMapper = id + UNDERSCORE + caseData.getApplicantCaseName();
        String caseSlaStartDateMapper = (String) caseData.getDateSubmitted();


        HearingDetails details = HearingDetails.automatedHearingDetailsWith()
            .autoListFlag(FALSE)
            .listingAutoChangeReasonCode("") // should right the code now ..
            .hearingType(HEARING_TYPE) // what is hearing type value see the recording ...
            .hearingWindow(
                HearingWindow.hearingWindowWith()
                    .dateRangeStart(caseData)  // which value needs to be set here ... there is no value coming from case data to set over here ... checked manage orders also no use ...
                    .dateRangeEnd(EMPTY)
                    .firstDateTimeMustBe(EMPTY)
                    .build())
            .duration(0)
            .hearingPriorityType(HEARING_PRIORITY)
            .numberOfPhysicalAttendees(0)
            .hearingInWelshFlag(FALSE)
            .hearingLocations(
                Arrays.asList(
                    HearingLocation.hearingLocationWith()
                        .locationType(COURT)
                        .locationId(CASE_MANAGEMENT_LOCATION)
                        .build()))
            .facilitiesRequired(Arrays.asList())
            .listingComments(EMPTY)
            .hearingRequester(EMPTY)
            .privateHearingRequiredFlag(privateHearingRequiredFlagMapper)
            .panelRequirements(null)
            .leadJudgeContractType(EMPTY)
            .hearingIsLinkedFlag(FALSE)
            .hearingChannels(Arrays.asList())
            .build();
        return details;
    }

        @NotNull
    private List<uk.gov.hmcts.reform.hmc.api.model.request.PartyDetails> getPartyDetails(CaseDetailResponse ccdResponse) {
        List<uk.gov.hmcts.reform.hmc.api.model.request.PartyDetails> partyDetailsList = new ArrayList<>();

        List<Element<PartyDetails>> applicantLst = ccdResponse.getCaseData().getApplicants();
        if (null != applicantLst) {
            partyDetailsList.addAll(addPartyData(applicantLst, APPLICANT));
        }

        List<Element<PartyDetails>> respondedLst = ccdResponse.getCaseData().getRespondents();
        if (null != respondedLst) {
            partyDetailsList.addAll(addPartyData(respondedLst, RESPONDENT));
        }

        PartyDetails applicantsFL401 = ccdResponse.getCaseData().getApplicantsFL401();
        if (null != applicantsFL401) {
            partyDetailsList.addAll(addFL401PartyData(applicantsFL401, APPLICANT));
        }

        PartyDetails respondentsFL401 = ccdResponse.getCaseData().getRespondentsFL401();
        if (null != respondentsFL401) {
            partyDetailsList.addAll(addFL401PartyData(respondentsFL401, RESPONDENT));
        }
        return partyDetailsList;
    }

    private List<uk.gov.hmcts.reform.hmc.api.model.request.PartyDetails> addPartyData(
        List<Element<PartyDetails>> partyLst, String role) {

        List<uk.gov.hmcts.reform.hmc.api.model.request.PartyDetails> partyDetailsList = new ArrayList<>();
        partyLst.forEach(p -> partyDetailsList.addAll(preparePartyDetailsDTO(p.getValue(), p.getId(), role)));

        return partyDetailsList;
    }

    private List<PartyFlagsModel> getPartyFlagsModel(PartyDetails partyDetails, UUID uuid) {
        String partyId = null;
        if (null != uuid) {
            partyId = uuid.toString();
        }
        List<PartyFlagsModel> partyFlagsModelList = new ArrayList<>();
        Flags flag = partyDetails.getPartyLevelFlag();
        if (flag == null) {
            return partyFlagsModelList;
        }
        List<Element<FlagDetail>> detailsList = flag.getDetails();

        if (detailsList != null) {
            for (Element<FlagDetail> flagDetailElement : detailsList) {
                FlagDetail flagDetail = flagDetailElement.getValue();
                if (null != flagDetail) {
                    log.info("flagDetail===> {}", flagDetail);
                    PartyFlagsModel partyFlagsModel =
                        PartyFlagsModel.partyFlagsModelWith()
                            .partyId(partyId)
                            .partyName(partyDetails.getFirstName() + EMPTY_STRING + partyDetails.getLastName())
                            .flagId(flagDetail.getFlagCode())
                            .flagStatus(flagDetail.getStatus())
                            .flagParentId(EMPTY)
                            .languageCode(flagDetail.getSubTypeKey())
                            .flagDescription(flagDetail.getFlagComment())
                            .build();
                    partyFlagsModelList.add(partyFlagsModel);
                }
            }
        }

        return partyFlagsModelList;
    }

    private List<uk.gov.hmcts.reform.hmc.api.model.request.PartyDetails> preparePartyDetailsDTO(PartyDetails partyDetails,
                                                                                                UUID uuid,
                                                                                                String role) {
        String partyId = null;
        if (null != uuid) {
            partyId = uuid.toString();
        }
        List<Element<FlagDetail>> flagsDetailOfCurrParty = null;

        if (null != partyDetails.getPartyLevelFlag()) {
            flagsDetailOfCurrParty = partyDetails.getPartyLevelFlag().getDetails();
        }
        List<PartyFlagsModel> curPartyFlagsModelList = getPartyFlagsModel(partyDetails, partyDetails.getPartyId());
        List<PartyFlagsModel> interpreterLangCodeList = getInterpreterLangCodes(curPartyFlagsModelList);

        String interpreterLanguageCode = EMPTY;
        if (interpreterLangCodeList.size() == ONE) {
            interpreterLanguageCode =
                (interpreterLangCodeList.get(0).getLanguageCode() != null)
                    ? interpreterLangCodeList.get(0).getLanguageCode()
                    : EMPTY;
        }
        Boolean isVulnerableFlag = false;
        String vulnerabilityDetails = "";
        List<String> reasonableAdjustments = null;

        if (null != flagsDetailOfCurrParty) {
            isVulnerableFlag = isVulnerableFlag(flagsDetailOfCurrParty);
            vulnerabilityDetails = getVulnerabilityDetails(flagsDetailOfCurrParty);
            reasonableAdjustments = getReasonableAdjustmentsByParty(flagsDetailOfCurrParty);
        }

        List<String> hearingChannelEmail =
            !isBlank(partyDetails.getEmail())
                ? Collections.singletonList(partyDetails.getEmail())
                : List.of();

        List<String> hearingChannelPhone =
            !isBlank(partyDetails.getPhoneNumber())
                ? Collections.singletonList(partyDetails.getPhoneNumber())
                : List.of();

        IndividualDetails individualDetails =
            IndividualDetails.builder()
                .firstName(partyDetails.getFirstName())
                .lastName(partyDetails.getLastName())
                .reasonableAdjustments(reasonableAdjustments)
                .vulnerableFlag(isVulnerableFlag)
                .vulnerabilityDetails(vulnerabilityDetails)
                .hearingChannelEmail(hearingChannelEmail)
                .hearingChannelPhone(hearingChannelPhone)
                .interpreterLanguage(interpreterLanguageCode)
                .relatedParties(List.of())
                .build();

        List<uk.gov.hmcts.reform.hmc.api.model.request.PartyDetails> partyDetailsList = new ArrayList<>();
        uk.gov.hmcts.reform.hmc.api.model.request.PartyDetails partyDetailsModel =
            uk.gov.hmcts.reform.hmc.api.model.request.PartyDetails.automatedPartyDetailsWith()
                .partyID(partyId)
                .partyType(PartyType.IND.name())
                .partyRole(role)
                .individualDetails(individualDetails)
                .build();

        partyDetailsList.add(partyDetailsModel);
        Organisation org = partyDetails.getSolicitorOrg();

        /****** Organisation Party Details********/
        if (org != null && org.getOrganisationID() != null) {
            partyDetailsList.add(addPartyDetailsModelForOrg(partyDetails, partyDetails.getSolicitorOrgUuid()));
        }

        /******Solicitor Party Details*********/
        if (partyDetails.getRepresentativeFirstName() != null || partyDetails.getRepresentativeLastName() != null) {
            uk.gov.hmcts.reform.hmc.api.model.request.PartyDetails details =
                addPartyDetailsModelForSolicitor(partyDetails, partyDetails.getSolicitorPartyId());
            if (null != details){
                partyDetailsList.add(details);
            }
        }
        return partyDetailsList;
    }


    protected List<PartyFlagsModel> getInterpreterLangCodes(
        List<PartyFlagsModel> curPartyFlagsModelList) {
        return curPartyFlagsModelList.stream()
            .filter(
                eachPartyFlag ->
                    eachPartyFlag.getFlagId().equals(RA0042)
                        || eachPartyFlag.getFlagId().equals(PF0015))
            .distinct()
            .toList();
    }

    protected String getVulnerabilityDetails(List<Element<FlagDetail>> flagsDetailOfCurrParty) {

        return flagsDetailOfCurrParty.stream()
            .filter(
                partyFlag ->
                    PF0002.equals(partyFlag.getValue().getFlagCode())
                        || PF0013.equals(partyFlag.getValue().getFlagCode())
                        || PF0018.equals(partyFlag.getValue().getFlagCode())
                        || SM0002.equals(partyFlag.getValue().getFlagCode()))
            .distinct()
            .map(p -> p.getValue().getName())
            .collect(Collectors.joining(PLUS_SIGN));
    }

    protected List<String> getReasonableAdjustmentsByParty(List<Element<FlagDetail>> flagsDetailOfCurrParty) {

        return flagsDetailOfCurrParty.stream()
            .filter(
                partyFlag ->
                    partyFlag.getValue().getFlagCode().startsWith(RA)
                        || partyFlag.getValue().getFlagCode().startsWith(SM))
            .distinct()
            .map(partyFlag -> partyFlag.getValue().getFlagCode())
            .toList();
    }

    protected uk.gov.hmcts.reform.hmc.api.model.request.PartyDetails addPartyDetailsModelForOrg(
        PartyDetails partyDetails, UUID uuid) {
        String partyId = null;
        if (uuid != null) {
            partyId = uuid.toString();
        }
        uk.gov.hmcts.reform.hmc.api.model.request.PartyDetails partyDetailsModelForOrg;
        OrganisationDetails organisationDetailsModel =
            OrganisationDetails.builder()
                .name(partyDetails.getSolicitorOrg().getOrganisationName())
                .cftOrganisationID(partyDetails.getSolicitorOrg().getOrganisationID())
                .organisationType(PartyType.ORG.toString())
                .build();

        partyDetailsModelForOrg =
            uk.gov.hmcts.reform.hmc.api.model.request.PartyDetails.automatedPartyDetailsWith()
                .partyID(partyId)
                //.partyName(partyDetails.getSolicitorOrg().getOrganisationName())
                .partyType(PartyType.ORG.name())
                .partyRole(ORGANISATION)
                .organisationDetails(organisationDetailsModel)
                .build();
        return partyDetailsModelForOrg;
    }

    protected uk.gov.hmcts.reform.hmc.api.model.request.PartyDetails addPartyDetailsModelForSolicitor(
        PartyDetails partyDetails, UUID uuid) {

        String partyId = null;
        if (uuid != null) {
            partyId = uuid.toString();
        }

        IndividualDetails individualDetails;
        uk.gov.hmcts.reform.hmc.api.model.request.PartyDetails partyDetailsModelForSol = null;

        List<String> hearingChannelEmail =
            !isBlank(partyDetails.getSolicitorEmail())
                ? Collections.singletonList(partyDetails.getSolicitorEmail())
                : List.of();

        if (!partyDetails.getRepresentativeFirstName().isBlank() && !partyDetails.getRepresentativeLastName().isBlank()) {
            individualDetails =
                IndividualDetails.builder()
                    .firstName(partyDetails.getRepresentativeFirstName())
                    .lastName(partyDetails.getRepresentativeLastName())
                    .hearingChannelEmail(hearingChannelEmail)
                    .build();

            partyDetailsModelForSol =
                uk.gov.hmcts.reform.hmc.api.model.request.PartyDetails.automatedPartyDetailsWith()
                    .partyID(partyId)
                    .partyType(PartyType.IND.name())
                    .partyRole(ORGANISATION)
                    .individualDetails(individualDetails)
                    .build();
        }
        return partyDetailsModelForSol;
    }

    protected Boolean isVulnerableFlag(List<Element<FlagDetail>> flagsDetailOfCurrParty) {

        return flagsDetailOfCurrParty.stream()
            .anyMatch(
                partyFlag ->
                    partyFlag.getValue().getFlagCode().equals(PF0002)
                        || PF0013.equals(partyFlag.getValue().getFlagCode())
                        || PF0018.equals(partyFlag.getValue().getFlagCode())
                        || SM0002.equals(partyFlag.getValue().getFlagCode()));
    }

    private List<uk.gov.hmcts.reform.hmc.api.model.request.PartyDetails> addFL401PartyData(
        PartyDetails partyDetails, String role) {

        List<uk.gov.hmcts.reform.hmc.api.model.request.PartyDetails> partyDetailsList = new ArrayList<>();
        if (null != partyDetails) {
            partyDetailsList.addAll(preparePartyDetailsDTO(partyDetails, partyDetails.getPartyId(), role));
        }
        return partyDetailsList;
    }

}
