package uk.gov.hmcts.reform.hmc.api.mapper;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseData;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Element;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Flags;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingChannelsEnum;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingData;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingPriorityTypeEnum;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingSpecificDatesOptionsEnum;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Organisation;
import uk.gov.hmcts.reform.hmc.api.model.ccd.PartyDetails;
import uk.gov.hmcts.reform.hmc.api.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.hmc.api.model.ccd.flagdata.FlagDetail;
import uk.gov.hmcts.reform.hmc.api.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.hmc.api.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingCaseCategories;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingCaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingPartyDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingRequest;
import uk.gov.hmcts.reform.hmc.api.model.request.IndividualDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.PanelRequirements;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingLocation;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingWindow;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyDetailsModel;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyFlagsModel;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyType;
import uk.gov.hmcts.reform.hmc.api.services.CaseFlagDataServiceImpl;
import uk.gov.hmcts.reform.hmc.api.utils.CaseUtils;

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
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.HMCTS_SERVICE_ID;
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

    private static boolean caseAdditionalSecurityFlag;

    private AutomatedHearingTransactionRequestMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static AutomatedHearingRequest mappingHearingTransactionRequest(CaseData caseData,String ccdBaseUrl) {

        String publicCaseNameMapper = EMPTY;
        if (C100.equals(CaseUtils.getCaseTypeOfApplication(caseData))) {
            publicCaseNameMapper = RE_MINOR;
        } else if (FL401.equals(CaseUtils.getCaseTypeOfApplication(caseData))) {
            PartyDetails applicantMap = caseData.getApplicantsFL401();
            PartyDetails respondentTableMap = caseData.getRespondentsFL401();
            publicCaseNameMapper = (applicantMap != null && respondentTableMap != null)
                ? applicantMap.getLastName() + AND + respondentTableMap.getLastName() : EMPTY;
        }

        List<AutomatedHearingPartyDetails> partyDetailsList = getPartyDetails(caseData);

        AutomatedHearingCaseDetails caseDetail = AutomatedHearingCaseDetails.automatedHearingCaseDetailsWith()
                .hmctsServiceCode(HMCTS_SERVICE_ID) //Hardcoded in prl-cos-api
                .caseRef(String.valueOf(caseData.getId()))
                // .requestTimeStamp(LocalDateTime.now())
                .externalCaseReference("") //Need to verify
                .caseDeepLink(ccdBaseUrl + caseData.getId() + CASE_FILE_VIEW)
                .hmctsInternalCaseName(caseData.getApplicantCaseName())
                .publicCaseName(publicCaseNameMapper)
                .caseAdditionalSecurityFlag(caseAdditionalSecurityFlag)
                .caseInterpreterRequiredFlag(caseData.getAttendHearing().getIsInterpreterNeeded())
                .caseCategories(getCaseCategories())
                .caseManagementLocationCode(caseData.getCaseManagementLocation().getBaseLocation())
                .caseRestrictedFlag(Boolean.FALSE) //Need to revisit what to set, If set to TRUE then can't access in LA
                .caseSlaStartDate(caseData.getIssueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .build();
        AutomatedHearingDetails hearingDetails = getHearingDetails(String.valueOf(caseData.getId()), caseData);
        AutomatedHearingRequest hearingRequest = AutomatedHearingRequest.automatedHearingRequestWith().build();
        hearingRequest.setPartyDetails(partyDetailsList);
        hearingRequest.setCaseDetails(caseDetail);
        hearingRequest.setHearingDetails(hearingDetails);

        return hearingRequest;

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

    private static AutomatedHearingDetails getHearingDetails(String id, CaseData caseData) {
        log.info("id: {}",id);
        HearingData hearingData = caseData.getHearingData();
        DynamicListElement hearingType = hearingData.getHearingTypes().getValue();

        return AutomatedHearingDetails.automatedHearingDetailsWith()
            .autoListFlag(StringUtils.isEmpty(hearingData.getAdditionalHearingDetails()))
            .hearingType(hearingType != null ? hearingType.getCode() : null)
            .hearingWindow(hearingWindow(hearingData))
            .duration(hearingDuration(
                hearingData.getHearingEstimatedDays(),
                hearingData.getHearingEstimatedHours(),
                hearingData.getHearingEstimatedMinutes()
            ))
            .hearingPriorityType(getHearingPriorityType(hearingData.getHearingPriorityTypeEnum()))
            .numberOfPhysicalAttendees(noOfPhysicalAttendees(
                hearingData.getAllPartiesAttendHearingSameWayYesOrNo(),
                hearingData,caseData
            ))
            .hearingInWelshFlag(caseData.getAttendHearing().getIsWelshNeeded())
            .hearingLocations(
                Collections.singletonList(
                    HearingLocation.hearingLocationWith()
                        .locationType(COURT)
                        .locationId(hearingData.getCourtList().getValueCode().split(":")[0]) //Extract court id without :
                        .build()))
            .facilitiesRequired(List.of())
            .listingComments(hearingData.getAdditionalHearingDetails())
            .hearingRequester(null != hearingData.getHearingJudgePersonalCode()
                                  ? hearingData.getHearingJudgePersonalCode() : EMPTY)
            .privateHearingRequiredFlag(C100.equals(CaseUtils.getCaseTypeOfApplication(caseData)))
            .panelRequirements(new PanelRequirements()) //Need to revisit
            .leadJudgeContractType("")
            .hearingIsLinkedFlag(hearingData.getHearingListedLinkedCases().getValue() != null)
            .hearingChannels(List.of(hearingData.getHearingChannelsEnum().getId()))
            .build();
    }

    private static String getHearingPriorityType(HearingPriorityTypeEnum hearingPriorityTypeEnum) {
        if (HearingPriorityTypeEnum.UrgentPriority.equals(hearingPriorityTypeEnum)) {
            return "Urgent";
        }
        return "Standard";
    }

    private static HearingWindow hearingWindow(HearingData hearingData) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        //specific date
        if (HearingSpecificDatesOptionsEnum.Yes.equals(hearingData.getHearingSpecificDatesOptionsEnum())) {
            return HearingWindow.hearingWindowWith()
                .firstDateTimeMustBe(hearingData.getFirstDateOfTheHearing() == null ? null :
                                         dateOfHearing(
                                             hearingData.getFirstDateOfTheHearing().format(formatter),
                                             hearingData.getHearingMustTakePlaceAtHour(),
                                             hearingData.getHearingMustTakePlaceAtMinute()
                                         ))
                .build();
        } else if (HearingSpecificDatesOptionsEnum.HearingRequiredBetweenCertainDates
                .equals(hearingData.getHearingSpecificDatesOptionsEnum())) {
            //date range
            return HearingWindow.hearingWindowWith()
                .dateRangeStart(hearingData.getEarliestHearingDate() != null
                                    ? hearingData.getEarliestHearingDate().format(formatter) : null)
                .dateRangeEnd(hearingData.getLatestHearingDate() != null ? hearingData.getLatestHearingDate().format(
                    formatter) : null)
                .build();
        }
        return null;
    }

    private static int hearingDuration(String days, String hours, String minutes) {

        if (Strings.isNotBlank(days)) {
            return Integer.parseInt(days) * 360;
        } else if (Strings.isNotBlank(hours)) {
            return Integer.parseInt(hours) * 60;
        } else if (Strings.isNotBlank(minutes)) {
            int remainder = Integer.parseInt(minutes) % 5;
            if (remainder != 0) {
                return (Integer.parseInt(minutes) / 5) * 5 + 5;
            }
        }
        return 0;
    }

    private static String dateOfHearing(@NotNull String firstDate, String hours, String minutes) {
        return String.format(
            "%sT%s:%s:00Z", firstDate,
            Strings.isNotBlank(hours) ? hours : "00",
            Strings.isNotBlank(minutes) ? minutes : "00"
        );
    }

    private static int noOfPhysicalAttendees(String attendSameWayYesOrNo, HearingData hearingData,CaseData caseData) {
        int totalParticipants = 0;
        if (YesOrNo.YES.name().equalsIgnoreCase(attendSameWayYesOrNo)
            && hearingData.getHearingChannelsEnum() == HearingChannelsEnum.INTER) {
            List<String> noOfParticipants = new ArrayList<>();
            if (C100.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                noOfParticipants.addAll(getParticipants(hearingData));
            } else if (FL401.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                noOfParticipants.addAll(Lists.newArrayList(hearingData.getApplicantName(),
                                                           hearingData.getApplicantSolicitor(),
                                                           hearingData.getRespondentName(),
                                                           hearingData.getRespondentSolicitor()));
            }
            noOfParticipants.removeAll(Arrays.asList("", null));
            totalParticipants = noOfParticipants.size();

        }
        if ("NO".equalsIgnoreCase(attendSameWayYesOrNo)) {
            List<DynamicList> noOfParticipants = new ArrayList<>();
            if (C100.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                noOfParticipants = getParticipantSelectedOptions(hearingData);
            } else if (FL401.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                noOfParticipants = Lists.newArrayList(hearingData.getApplicantHearingChannel(),
                                                           hearingData.getApplicantSolicitorHearingChannel(),
                                                           hearingData.getRespondentHearingChannel(),
                                                           hearingData.getRespondentSolicitorHearingChannel(),
                                                           hearingData.getLocalAuthorityHearingChannel());
            }

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

    private static List<DynamicList> getParticipantSelectedOptions(HearingData hearingData) {
        List<DynamicList> participants = new ArrayList<>();

        if (hearingData.getHearingDataApplicantDetails() != null) {
            participants.addAll(Lists.newArrayList(
                hearingData.getHearingDataApplicantDetails().getApplicantHearingChannel1(),
                hearingData.getHearingDataApplicantDetails().getApplicantHearingChannel2(),
                hearingData.getHearingDataApplicantDetails().getApplicantHearingChannel3(),
                hearingData.getHearingDataApplicantDetails().getApplicantHearingChannel4(),
                hearingData.getHearingDataApplicantDetails().getApplicantHearingChannel5(),
                hearingData.getHearingDataApplicantDetails().getApplicantSolicitorHearingChannel1(),
                hearingData.getHearingDataApplicantDetails().getApplicantSolicitorHearingChannel2(),
                hearingData.getHearingDataApplicantDetails().getApplicantSolicitorHearingChannel3(),
                hearingData.getHearingDataApplicantDetails().getApplicantSolicitorHearingChannel4(),
                hearingData.getHearingDataApplicantDetails().getApplicantSolicitorHearingChannel5()
            ));
        }
        if (hearingData.getHearingDataRespondentDetails() != null) {
            participants.addAll(Lists.newArrayList(
                hearingData.getHearingDataRespondentDetails().getRespondentHearingChannel1(),
                hearingData.getHearingDataRespondentDetails().getRespondentHearingChannel2(),
                hearingData.getHearingDataRespondentDetails().getRespondentHearingChannel3(),
                hearingData.getHearingDataRespondentDetails().getRespondentHearingChannel4(),
                hearingData.getHearingDataRespondentDetails().getRespondentHearingChannel5(),
                hearingData.getHearingDataRespondentDetails().getRespondentSolicitorHearingChannel1(),
                hearingData.getHearingDataRespondentDetails().getRespondentSolicitorHearingChannel2(),
                hearingData.getHearingDataRespondentDetails().getRespondentSolicitorHearingChannel3(),
                hearingData.getHearingDataRespondentDetails().getRespondentSolicitorHearingChannel4(),
                hearingData.getHearingDataRespondentDetails().getRespondentSolicitorHearingChannel5()
            ));
        }
        if (hearingData.getLocalAuthorityHearingChannel() != null) {
            participants.add(hearingData.getLocalAuthorityHearingChannel());
        }
        if (hearingData.getCafcassCymruHearingChannel() != null) {
            participants.add(hearingData.getCafcassCymruHearingChannel());
        }

        if (hearingData.getCafcassHearingChannel() != null) {
            participants.add(hearingData.getCafcassHearingChannel());
        }
        return participants;
    }

    private static List<String> getParticipants(HearingData hearingData) {
        List<String> noOfParticipants = new ArrayList<>();
        if (hearingData.getHearingDataApplicantDetails() != null) {
            noOfParticipants.addAll(Lists.newArrayList(
                hearingData.getHearingDataApplicantDetails().getApplicantName1(),
                hearingData.getHearingDataApplicantDetails().getApplicantName2(),
                hearingData.getHearingDataApplicantDetails().getApplicantName3(),
                hearingData.getHearingDataApplicantDetails().getApplicantName4(),
                hearingData.getHearingDataApplicantDetails().getApplicantName5(),
                hearingData.getHearingDataApplicantDetails().getApplicantSolicitor1(),
                hearingData.getHearingDataApplicantDetails().getApplicantSolicitor2(),
                hearingData.getHearingDataApplicantDetails().getApplicantSolicitor3(),
                hearingData.getHearingDataApplicantDetails().getApplicantSolicitor4(),
                hearingData.getHearingDataApplicantDetails().getApplicantSolicitor5()
            ));
        }
        if (hearingData.getHearingDataRespondentDetails() != null) {
            noOfParticipants.addAll(Lists.newArrayList(
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
            ));
        }
        return noOfParticipants;
    }

    @NotNull
    private static List<AutomatedHearingPartyDetails>
        getPartyDetails(CaseData caseData) {
        List<PartyFlagsModel> partiesFlagsModelList = new ArrayList<>();
        List<PartyDetailsModel> partyDetailsModelList = new ArrayList<>();
        List<AutomatedHearingPartyDetails> partyDetailsList = new ArrayList<>();
        if (C100.equals(CaseUtils.getCaseTypeOfApplication(caseData))) {
            List<Element<PartyDetails>> applicantLst = caseData.getApplicants();
            if (null != applicantLst) {
                partyDetailsList.addAll(addPartyData(applicantLst, APPLICANT));
                CaseFlagDataServiceImpl.addPartyFlagData(partiesFlagsModelList, partyDetailsModelList, applicantLst, APPLICANT);
            }
            List<Element<PartyDetails>> respondedLst = caseData.getRespondents();
            if (null != respondedLst) {
                partyDetailsList.addAll(addPartyData(respondedLst, RESPONDENT));
                CaseFlagDataServiceImpl.addPartyFlagData(partiesFlagsModelList, partyDetailsModelList, respondedLst, RESPONDENT);
            }
        } else if (FL401.equals(CaseUtils.getCaseTypeOfApplication(caseData))) {
            PartyDetails applicantsFL401 = caseData.getApplicantsFL401();
            if (null != applicantsFL401) {
                partyDetailsList.addAll(addFL401PartyData(applicantsFL401, APPLICANT));
                CaseFlagDataServiceImpl.addFL401PartyFlagData(
                    partiesFlagsModelList, partyDetailsModelList, applicantsFL401, APPLICANT);
            }
            PartyDetails respondentsFL401 = caseData.getRespondentsFL401();
            if (null != respondentsFL401) {
                partyDetailsList.addAll(addFL401PartyData(respondentsFL401, RESPONDENT));
                CaseFlagDataServiceImpl.addFL401PartyFlagData(
                    partiesFlagsModelList, partyDetailsModelList, respondentsFL401, RESPONDENT);
            }
        }

        if (!partiesFlagsModelList.isEmpty() || !partyDetailsModelList.isEmpty()) {
            caseAdditionalSecurityFlag = CaseFlagDataServiceImpl.isCaseAdditionalSecurityFlag(partiesFlagsModelList);
        }
        return partyDetailsList;
    }

    private static List<AutomatedHearingPartyDetails> addPartyData(List<Element<PartyDetails>> partyLst, String role) {

        List<AutomatedHearingPartyDetails> partyDetailsList = new ArrayList<>();
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

    private static List<AutomatedHearingPartyDetails> preparePartyDetailsDTO(
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

        List<AutomatedHearingPartyDetails> partyDetailsList = new ArrayList<>();
        AutomatedHearingPartyDetails partyDetailsModel = AutomatedHearingPartyDetails.automatedHearingPartyDetailsWith()
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
            AutomatedHearingPartyDetails details =
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

    private static AutomatedHearingPartyDetails addPartyDetailsModelForOrg(
        PartyDetails partyDetails, UUID uuid) {
        String partyId = null;
        if (uuid != null) {
            partyId = uuid.toString();
        }
        AutomatedHearingPartyDetails partyDetailsModelForOrg;
        OrganisationDetails organisationDetailsModel = OrganisationDetails.builder()
                .name(partyDetails.getSolicitorOrg().getOrganisationName())
                .cftOrganisationID(partyDetails.getSolicitorOrg().getOrganisationID())
                .organisationType(PartyType.ORG.toString())
                .build();

        partyDetailsModelForOrg = AutomatedHearingPartyDetails.automatedHearingPartyDetailsWith()
                .partyID(partyId)
                //.partyName(partyDetails.getSolicitorOrg().getOrganisationName())
                .partyType(PartyType.ORG.name())
                .partyRole(ORGANISATION)
                .organisationDetails(organisationDetailsModel)
                .build();
        return partyDetailsModelForOrg;
    }

    private static AutomatedHearingPartyDetails addPartyDetailsModelForSolicitor(
        PartyDetails partyDetails, UUID uuid) {

        String partyId = null;
        if (uuid != null) {
            partyId = uuid.toString();
        }

        IndividualDetails individualDetails;
        AutomatedHearingPartyDetails partyDetailsModelForSol = null;

        List<String> hearingChannelEmail =
            !isBlank(partyDetails.getSolicitorEmail())
                ? Collections.singletonList(partyDetails.getSolicitorEmail())
                : List.of();

        if (!partyDetails.getRepresentativeFirstName().isBlank()
            && !partyDetails.getRepresentativeLastName().isBlank()) {
            individualDetails =
                IndividualDetails.builder()
                    .firstName(partyDetails.getRepresentativeFirstName())
                    .lastName(partyDetails.getRepresentativeLastName())
                    .hearingChannelEmail(hearingChannelEmail)
                    .build();

            partyDetailsModelForSol = AutomatedHearingPartyDetails.automatedHearingPartyDetailsWith()
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

    private static List<AutomatedHearingPartyDetails> addFL401PartyData(
        PartyDetails partyDetails, String role) {

        List<AutomatedHearingPartyDetails> partyDetailsList = new ArrayList<>();
        if (null != partyDetails) {
            partyDetailsList.addAll(preparePartyDetailsDTO(partyDetails, partyDetails.getPartyId(), role));
        }
        return partyDetailsList;
    }

}
