package uk.gov.hmcts.reform.hmc.api.mapper;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseData;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Element;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Flags;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingChannelsEnum;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingData;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Organisation;
import uk.gov.hmcts.reform.hmc.api.model.ccd.PartyDetails;
import uk.gov.hmcts.reform.hmc.api.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.hmc.api.model.ccd.flagdata.FlagDetail;
import uk.gov.hmcts.reform.hmc.api.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.hmc.api.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingCaseCategories;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingRequest;
import uk.gov.hmcts.reform.hmc.api.model.request.IndividualDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingLocation;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingWindow;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyFlagsModel;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.AND;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.APPLICANT;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.C100;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_FILE_VIEW;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_SUB_TYPE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_TYPE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CATEGORY_VALUE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.COURT;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.EMPTY;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.EMPTY_STRING;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.FL401;
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

@Slf4j
public final class AutomatedHearingTransactionRequestMapper {
    @Value("${ccd.ui.url}")
    private static String ccdBaseUrl;

    private AutomatedHearingTransactionRequestMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static List<AutomatedHearingRequest> mappingHearingTransactionRequest(CaseData caseData) {

        String publicCaseNameMapper = EMPTY;
        if (C100.equals(caseData.getCaseTypeOfApplication())) {
            publicCaseNameMapper = RE_MINOR;
        } else if (FL401.equals(caseData.getCaseTypeOfApplication())) {
            PartyDetails applicantMap = caseData.getApplicantsFL401();
            PartyDetails respondentTableMap = caseData.getRespondentsFL401();
            publicCaseNameMapper = (applicantMap != null && respondentTableMap != null)
                ? applicantMap.getLastName() + AND + respondentTableMap.getLastName() : EMPTY;
        }

        uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingCaseDetails caseDetail =
            uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingCaseDetails.automatedHearingCaseDetailsWith()
                .hmctsServiceCode("ABA5") //Hardcoded in prl-cos-api
                .caseRef(String.valueOf(caseData.getId()))
                .requestTimeStamp(LocalDateTime.now())
                .externalCaseReference("") //Need to verify
                .caseDeepLink(ccdBaseUrl + "caseReference" + CASE_FILE_VIEW) //Need to verify
                .hmctsInternalCaseName("")
                .publicCaseName(publicCaseNameMapper)
                .caseAdditionalSecurityFlag(Boolean.TRUE) //1
                .caseInterpreterRequiredFlag(Boolean.TRUE) // 2
                .caseCategories(getCaseCategories())
                .caseManagementLocationCode("") // 3
                .caseRestrictedFlag(Boolean.TRUE) // 4
                .caseSlaStartDate(caseData.getIssueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .build();
        List<uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingPartyDetails> partyDetailsList = getPartyDetails(
            caseData);
        List<AutomatedHearingRequest> hearingRequests = new ArrayList<>();
        List<AutomatedHearingDetails> hearingDetails = getHearingDetails(String.valueOf(caseData.getId()), caseData);
        for (AutomatedHearingDetails details : hearingDetails) {
            AutomatedHearingRequest hearingRequest = AutomatedHearingRequest.automatedHearingRequestWith().build();
            hearingRequest.setPartyDetails(partyDetailsList);
            hearingRequest.setCaseDetails(caseDetail);
            hearingRequest.setHearingDetails(details);
            hearingRequests.add(hearingRequest);
        }
        return hearingRequests;

    }

    private static List<AutomatedHearingCaseCategories> getCaseCategories() {
        List<AutomatedHearingCaseCategories> caseCategoriesList = new ArrayList<>();
        AutomatedHearingCaseCategories caseCategories =
            AutomatedHearingCaseCategories.AutomatedHearingCaseCategoriesWith()
                .categoryType(CASE_TYPE)
                .categoryValue(CATEGORY_VALUE)
                .build();

        AutomatedHearingCaseCategories caseSubCategories =
            AutomatedHearingCaseCategories.AutomatedHearingCaseCategoriesWith()
                .categoryType(CASE_SUB_TYPE)
                .categoryValue(CATEGORY_VALUE)
                .categoryParent(CATEGORY_VALUE)
                .build();

        caseCategoriesList.add(caseCategories);
        caseCategoriesList.add(caseSubCategories);
        return caseCategoriesList;
    }

    private static List<AutomatedHearingDetails> getHearingDetails(String id, CaseData caseData) {
        log.info("id: {}",id);
        List<AutomatedHearingDetails> hearingDetailsList = new ArrayList<>();
        List<Element<HearingData>> headingDetailsList = caseData.getManageOrders().getOrdersHearingDetails();

        for (Element<HearingData> hearingDataEle : headingDetailsList) {
            HearingData hearingData = hearingDataEle.getValue();
            DynamicListElement hearingType = hearingData.getHearingTypes().getValue();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            AutomatedHearingDetails details = AutomatedHearingDetails.automatedHearingDetailsWith()
                .autoListFlag(StringUtils.isEmpty(hearingData.getAdditionalHearingDetails()))
                .listingAutoChangeReasonCode("")
                .hearingType(hearingType != null ? hearingType.getCode() : null)
                .hearingWindow(
                    HearingWindow.hearingWindowWith()
                        .dateRangeStart(hearingData.getEarliestHearingDate() != null
                                            ? hearingData.getEarliestHearingDate().format(
                            formatter) : null)
                        .dateRangeEnd(hearingData.getLatestHearingDate() != null ? hearingData.getLatestHearingDate().format(
                            formatter) : null)
                        .firstDateTimeMustBe(hearingData.getFirstDateOfTheHearing() == null ? null :
                                                 dateOfHearing(
                                                     hearingData.getFirstDateOfTheHearing().format(formatter),
                                                     hearingData.getHearingMustTakePlaceAtHour(),
                                                     hearingData.getHearingMustTakePlaceAtMinute()
                                                 ))
                        .build())
                .duration(hearingDuration(
                    hearingData.getHearingEstimatedDays(),
                    hearingData.getHearingEstimatedHours(),
                    hearingData.getHearingEstimatedMinutes()
                ))
                .hearingPriorityType(hearingData.getHearingPriorityTypeEnum().getDisplayedValue())
                .numberOfPhysicalAttendees(noOfPhysicalAttendees(
                    hearingData.getAllPartiesAttendHearingSameWayYesOrNo(),
                    hearingData
                )) // this is complex logic need to write
                .hearingInWelshFlag(caseData.getAttendHearing().getIsWelshNeeded())
                .hearingLocations(
                    Collections.singletonList(
                        HearingLocation.hearingLocationWith()
                            .locationType(COURT)
                            .locationId(hearingData.getCourtList().getValueCode())
                            .build()))
                .facilitiesRequired(List.of())
                .listingComments(hearingData.getAdditionalHearingDetails())
                .hearingRequester(hearingData.getHearingJudgePersonalCode())
                .privateHearingRequiredFlag(C100.equals(caseData.getCaseTypeOfApplication()))
                .panelRequirements(null)
                .leadJudgeContractType("")
                .hearingIsLinkedFlag(hearingData.getHearingListedLinkedCases() != null)
                .hearingChannels(List.of(hearingData.getHearingChannelsEnum().getDisplayedValue()))
                .build();
            hearingDetailsList.add(details);
        }
        return hearingDetailsList;
    }

    private static int hearingDuration(String days, String hours, String minutes) {

        if (days != null) {
            return Integer.parseInt(days) * 360;
        } else if (hours != null) {
            return Integer.parseInt(hours) * 60;
        } else if (minutes != null) {
            int remainder = Integer.parseInt(minutes) % 5;
            if (remainder != 0) {
                return (Integer.parseInt(minutes) / 5) * 5 + 5;
            }
        }
        return 0;
    }

    private static String dateOfHearing(@NotNull String firstDate, String hours, String minutes) {
        return
            String.format("{0}T{1}:{2}:00Z", firstDate, hours != null ? hours : "00", minutes != null ? minutes : "00");

    }

    private static int noOfPhysicalAttendees(YesOrNo attendSameWayYesOrNo, HearingData hearingData) {
        int totalParticipants = 0;
        if (YesOrNo.YES.equals(attendSameWayYesOrNo) && hearingData.getHearingChannelsEnum() == HearingChannelsEnum.INTER) {
            ArrayList<String> noOfParticipants = Lists.newArrayList(
                                   hearingData.getHearingDataApplicantDetails().getApplicantName1(),
                                   hearingData.getHearingDataApplicantDetails().getApplicantName2(),
                                   hearingData.getHearingDataApplicantDetails().getApplicantName3(),
                                   hearingData.getHearingDataApplicantDetails().getApplicantName4(),
                                   hearingData.getHearingDataApplicantDetails().getApplicantName5(),
                                   hearingData.getHearingDataApplicantDetails().getApplicantSolicitor1(),
                                   hearingData.getHearingDataApplicantDetails().getApplicantSolicitor2(),
                                   hearingData.getHearingDataApplicantDetails().getApplicantSolicitor3(),
                                   hearingData.getHearingDataApplicantDetails().getApplicantSolicitor4(),
                                   hearingData.getHearingDataApplicantDetails().getApplicantSolicitor5(),
                                   hearingData.getHearingDataRespondentDetails().getRespondentName1(),
                                   hearingData.getHearingDataRespondentDetails().getRespondentName2(),
                                   hearingData.getHearingDataRespondentDetails().getRespondentName3(),
                                   hearingData.getHearingDataRespondentDetails().getRespondentName4(),
                                   hearingData.getHearingDataRespondentDetails().getRespondentName5(),
                                   hearingData.getHearingDataRespondentDetails().getRespondentSolicitor1(),
                                   hearingData.getHearingDataRespondentDetails().getRespondentSolicitor2(),
                                   hearingData.getHearingDataRespondentDetails().getRespondentSolicitor3(),
                                   hearingData.getHearingDataRespondentDetails().getRespondentSolicitor4(),
                                   hearingData.getHearingDataRespondentDetails().getRespondentSolicitor5()
                );
            noOfParticipants.removeAll(Arrays.asList("", null));
            totalParticipants = noOfParticipants.size();
        }
        if (YesOrNo.NO.equals(attendSameWayYesOrNo)) {
            ArrayList<DynamicList> noOfParticipants = Lists.newArrayList(
                                   hearingData.getHearingDataApplicantDetails().getApplicantHearingChannel1(),
                                   hearingData.getHearingDataApplicantDetails().getApplicantHearingChannel2(),
                                   hearingData.getHearingDataApplicantDetails().getApplicantHearingChannel3(),
                                   hearingData.getHearingDataApplicantDetails().getApplicantHearingChannel4(),
                                   hearingData.getHearingDataApplicantDetails().getApplicantHearingChannel5(),
                                   hearingData.getHearingDataApplicantDetails().getApplicantSolicitorHearingChannel1(),
                                   hearingData.getHearingDataApplicantDetails().getApplicantSolicitorHearingChannel2(),
                                   hearingData.getHearingDataApplicantDetails().getApplicantSolicitorHearingChannel3(),
                                   hearingData.getHearingDataApplicantDetails().getApplicantSolicitorHearingChannel4(),
                                   hearingData.getHearingDataApplicantDetails().getApplicantSolicitorHearingChannel5(),
                                   hearingData.getLocalAuthorityHearingChannel(),
                                   hearingData.getCafcassCymruHearingChannel(),
                                   hearingData.getHearingDataRespondentDetails().getRespondentHearingChannel1(),
                                   hearingData.getHearingDataRespondentDetails().getRespondentHearingChannel2(),
                                   hearingData.getHearingDataRespondentDetails().getRespondentHearingChannel3(),
                                   hearingData.getHearingDataRespondentDetails().getRespondentHearingChannel4(),
                                   hearingData.getHearingDataRespondentDetails().getRespondentHearingChannel5(),
                                   hearingData.getHearingDataRespondentDetails()
                                       .getRespondentSolicitorHearingChannel1(),
                                   hearingData.getHearingDataRespondentDetails()
                                       .getRespondentSolicitorHearingChannel2(),
                                   hearingData.getHearingDataRespondentDetails()
                                       .getRespondentSolicitorHearingChannel3(),
                                   hearingData.getHearingDataRespondentDetails()
                                       .getRespondentSolicitorHearingChannel4(),
                                   hearingData.getHearingDataRespondentDetails().getRespondentSolicitorHearingChannel5()
                );

            Map<Boolean, List<String>> selectedCodes = noOfParticipants.stream().filter(Objects::nonNull)
                .map(DynamicList::getValueCode)
                .filter(StringUtils::isNotBlank).collect(Collectors.partitioningBy(p -> HearingChannelsEnum.INTER.name()
                    .equals(p)));
            int inPersonCount = selectedCodes.get(Boolean.TRUE).size();
            int otherCount = selectedCodes.get(Boolean.FALSE).size();
            return hearingData.getHearingChannelsEnum() == HearingChannelsEnum.INTER ? totalParticipants - otherCount :
                inPersonCount;
        }
        return totalParticipants;
    }

    @NotNull
    private static List<uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingPartyDetails>
        getPartyDetails(CaseData caseData) {
        List<uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingPartyDetails> partyDetailsList = new
            ArrayList<>();

        List<Element<PartyDetails>> applicantLst = caseData.getApplicants();
        if (null != applicantLst) {
            partyDetailsList.addAll(addPartyData(applicantLst, APPLICANT));
        }

        List<Element<PartyDetails>> respondedLst = caseData.getRespondents();
        if (null != respondedLst) {
            partyDetailsList.addAll(addPartyData(respondedLst, RESPONDENT));
        }

        PartyDetails applicantsFL401 = caseData.getApplicantsFL401();
        if (null != applicantsFL401) {
            partyDetailsList.addAll(addFL401PartyData(applicantsFL401, APPLICANT));
        }

        PartyDetails respondentsFL401 = caseData.getRespondentsFL401();
        if (null != respondentsFL401) {
            partyDetailsList.addAll(addFL401PartyData(respondentsFL401, RESPONDENT));
        }
        return partyDetailsList;
    }

    private static List<uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingPartyDetails> addPartyData(
        List<Element<PartyDetails>> partyLst, String role) {

        List<uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingPartyDetails> partyDetailsList = new ArrayList<>();
        partyLst.forEach(p -> partyDetailsList.addAll(preparePartyDetailsDTO(p.getValue(), p.getId(), role)));

        return partyDetailsList;
    }

    private static List<PartyFlagsModel> getPartyFlagsModel(PartyDetails partyDetails, UUID uuid) {
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

    private static List<uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingPartyDetails> preparePartyDetailsDTO(
        PartyDetails partyDetails, UUID uuid, String role) {
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

        List<uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingPartyDetails> partyDetailsList = new ArrayList<>();
        uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingPartyDetails partyDetailsModel =
            uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingPartyDetails.automatedHearingPartyDetailsWith()
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
            uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingPartyDetails details =
                addPartyDetailsModelForSolicitor(partyDetails, partyDetails.getSolicitorPartyId());
            if (null != details) {
                partyDetailsList.add(details);
            }
        }
        return partyDetailsList;
    }


    private static List<PartyFlagsModel> getInterpreterLangCodes(
        List<PartyFlagsModel> curPartyFlagsModelList) {
        return curPartyFlagsModelList.stream()
            .filter(
                eachPartyFlag ->
                    eachPartyFlag.getFlagId().equals(RA0042)
                        || eachPartyFlag.getFlagId().equals(PF0015))
            .distinct()
            .toList();
    }

    private static String getVulnerabilityDetails(List<Element<FlagDetail>> flagsDetailOfCurrParty) {

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

    private static List<String> getReasonableAdjustmentsByParty(List<Element<FlagDetail>> flagsDetailOfCurrParty) {

        return flagsDetailOfCurrParty.stream()
            .filter(
                partyFlag ->
                    partyFlag.getValue().getFlagCode().startsWith(RA)
                        || partyFlag.getValue().getFlagCode().startsWith(SM))
            .distinct()
            .map(partyFlag -> partyFlag.getValue().getFlagCode())
            .toList();
    }

    private static uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingPartyDetails addPartyDetailsModelForOrg(
        PartyDetails partyDetails, UUID uuid) {
        String partyId = null;
        if (uuid != null) {
            partyId = uuid.toString();
        }
        uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingPartyDetails partyDetailsModelForOrg;
        OrganisationDetails organisationDetailsModel =
            OrganisationDetails.builder()
                .name(partyDetails.getSolicitorOrg().getOrganisationName())
                .cftOrganisationID(partyDetails.getSolicitorOrg().getOrganisationID())
                .organisationType(PartyType.ORG.toString())
                .build();

        partyDetailsModelForOrg =
            uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingPartyDetails.automatedHearingPartyDetailsWith()
                .partyID(partyId)
                //.partyName(partyDetails.getSolicitorOrg().getOrganisationName())
                .partyType(PartyType.ORG.name())
                .partyRole(ORGANISATION)
                .organisationDetails(organisationDetailsModel)
                .build();
        return partyDetailsModelForOrg;
    }

    private static uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingPartyDetails addPartyDetailsModelForSolicitor(
        PartyDetails partyDetails, UUID uuid) {

        String partyId = null;
        if (uuid != null) {
            partyId = uuid.toString();
        }

        IndividualDetails individualDetails;
        uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingPartyDetails partyDetailsModelForSol = null;

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
                uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingPartyDetails.automatedHearingPartyDetailsWith()
                    .partyID(partyId)
                    .partyType(PartyType.IND.name())
                    .partyRole(ORGANISATION)
                    .individualDetails(individualDetails)
                    .build();
        }
        return partyDetailsModelForSol;
    }

    private static Boolean isVulnerableFlag(List<Element<FlagDetail>> flagsDetailOfCurrParty) {

        return flagsDetailOfCurrParty.stream()
            .anyMatch(
                partyFlag ->
                    partyFlag.getValue().getFlagCode().equals(PF0002)
                        || PF0013.equals(partyFlag.getValue().getFlagCode())
                        || PF0018.equals(partyFlag.getValue().getFlagCode())
                        || SM0002.equals(partyFlag.getValue().getFlagCode()));
    }

    private static List<uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingPartyDetails> addFL401PartyData(
        PartyDetails partyDetails, String role) {

        List<uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingPartyDetails> partyDetailsList = new ArrayList<>();
        if (null != partyDetails) {
            partyDetailsList.addAll(preparePartyDetailsDTO(partyDetails, partyDetails.getPartyId(), role));
        }
        return partyDetailsList;
    }

}
