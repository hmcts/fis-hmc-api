package uk.gov.hmcts.reform.hmc.api.services;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseData;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Element;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingChannelsEnum;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingData;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingPriorityTypeEnum;
import uk.gov.hmcts.reform.hmc.api.model.ccd.HearingSpecificDatesOptionsEnum;
import uk.gov.hmcts.reform.hmc.api.model.ccd.PartyDetails;
import uk.gov.hmcts.reform.hmc.api.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.hmc.api.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.hmc.api.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingCaseCategories;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingCaseDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingPartyDetails;
import uk.gov.hmcts.reform.hmc.api.model.request.AutomatedHearingRequest;
import uk.gov.hmcts.reform.hmc.api.model.request.PanelRequirements;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingLocation;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingWindow;
import uk.gov.hmcts.reform.hmc.api.model.response.IndividualDetailsModel;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyDetailsModel;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyFlagsModel;
import uk.gov.hmcts.reform.hmc.api.utils.CaseUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.AND;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.APPLICANT;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.C100;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_FILE_VIEW;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_SUB_TYPE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CASE_TYPE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.CATEGORY_VALUE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.COURT;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.EMPTY;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.FL401;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.HMCTS_SERVICE_ID;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.RESPONDENT;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.RE_MINOR;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.YES;

@Slf4j
@Service
@AllArgsConstructor
public class AutomatedHearingService {

    private final CaseFlagV2DataServiceImpl caseFlagService;

    public static final String LOCAL_AUTHORITY = "Local Authority";

    public AutomatedHearingRequest mapCaseDataToAutoHearingRequest(CaseData caseData, String ccdBaseUrl) {

        String publicCaseNameMapper = EMPTY;
        if (C100.equals(CaseUtils.getCaseTypeOfApplication(caseData))) {
            publicCaseNameMapper = RE_MINOR;
        } else if (FL401.equals(CaseUtils.getCaseTypeOfApplication(caseData))) {
            PartyDetails applicantMap = caseData.getApplicantsFL401();
            PartyDetails respondentTableMap = caseData.getRespondentsFL401();
            publicCaseNameMapper = (applicantMap != null && respondentTableMap != null)
                ? applicantMap.getLastName() + AND + respondentTableMap.getLastName() : EMPTY;
        }

        List<PartyFlagsModel> partyFlags = new ArrayList<>();
        List<PartyDetailsModel> partyDetails = new ArrayList<>();

        caseFlagService.setPartyDetailsLists(partyFlags, partyDetails, caseData);

        partyDetails.forEach(partyDetailsModel -> updatePreferredHearingChannel(partyDetailsModel, caseData));
        List<AutomatedHearingPartyDetails> partyDetailsList = partyDetails.stream()
            .map(AutomatedHearingPartyDetails::fromPartyDetailsModel)
            .toList();

        Boolean caseAdditionalSecurityFlag = CaseFlagDataServiceImpl.isCaseAdditionalSecurityFlag(partyFlags);

        AutomatedHearingCaseDetails caseDetail = AutomatedHearingCaseDetails.automatedHearingCaseDetailsWith()
                .hmctsServiceCode(HMCTS_SERVICE_ID) //Hardcoded in prl-cos-api
                .caseRef(String.valueOf(caseData.getId()))
                // .requestTimeStamp(LocalDateTime.now())
                .externalCaseReference("") //Need to verify
                .caseDeepLink(ccdBaseUrl + caseData.getId() + CASE_FILE_VIEW)
                .hmctsInternalCaseName(caseData.getApplicantCaseName())
                .publicCaseName(publicCaseNameMapper)
                .caseAdditionalSecurityFlag(caseAdditionalSecurityFlag)
                .caseInterpreterRequiredFlag(YES.equalsIgnoreCase(caseData.getAttendHearing().getIsInterpreterNeeded()))
                .caseCategories(getCaseCategories())
                .caseManagementLocationCode(caseData.getCaseManagementLocation().getBaseLocation())
                .caseRestrictedFlag(Boolean.FALSE) //Need to revisit what to set, If set to TRUE then can't access in LA
                .caseSlaStartDate(caseData.getIssueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .build();
        AutomatedHearingDetails hearingDetails = getHearingDetails(caseData);
        AutomatedHearingRequest hearingRequest = AutomatedHearingRequest.automatedHearingRequestWith().build();
        hearingRequest.setPartyDetails(partyDetailsList);
        hearingRequest.setCaseDetails(caseDetail);
        hearingRequest.setHearingDetails(hearingDetails);

        return hearingRequest;

    }

    private Optional<Integer> findPartyIndex(String partyId, List<Element<PartyDetails>> parties) {
        Optional<Element<PartyDetails>> partyDetailsElement = parties.stream()
            .filter(el -> UUID.fromString(partyId).equals(el.getValue().getPartyId())
                || UUID.fromString(partyId).equals(el.getValue().getSolicitorPartyId()))
            .findFirst();

        return partyDetailsElement.map(parties::indexOf);
    }

    private boolean isSolicitor(String partyId, PartyDetails partyDetails) {
        return UUID.fromString(partyId).equals(partyDetails.getSolicitorPartyId());
    }

    private String getPreferredHearingChannelC100(PartyDetailsModel partyDetailsModel, CaseData caseData) {
        if (partyDetailsModel.getPartyRole().equalsIgnoreCase(APPLICANT)) {
            Optional<Integer> partyIndex = findPartyIndex(partyDetailsModel.getPartyID(), caseData.getApplicants());
            if (partyIndex.isPresent()) {
                boolean isSolicitor = isSolicitor(partyDetailsModel.getPartyID(),
                                                  caseData.getApplicants().get(partyIndex.get()).getValue());
                return getPreferredHearingChannel(partyIndex.get(), APPLICANT, isSolicitor, caseData.getHearingData());
            }
        } else if  (partyDetailsModel.getPartyRole().equalsIgnoreCase(RESPONDENT)) {
            Optional<Integer> partyIndex = findPartyIndex(partyDetailsModel.getPartyID(), caseData.getRespondents());
            if (partyIndex.isPresent()) {
                boolean isSolicitor = isSolicitor(partyDetailsModel.getPartyID(),
                                                  caseData.getRespondents().get(partyIndex.get()).getValue());
                return getPreferredHearingChannel(partyIndex.get(), RESPONDENT, isSolicitor, caseData.getHearingData());
            }
        }
        return null;
    }

    private String getPreferredHearingChannelFL401(PartyDetailsModel partyDetailsModel, CaseData caseData) {
        if (partyDetailsModel.getPartyRole().equalsIgnoreCase(APPLICANT)) {
            boolean isSolicitor = isSolicitor(partyDetailsModel.getPartyID(), caseData.getRespondentsFL401());
            return getPreferredHearingChannel(-1, APPLICANT, isSolicitor, caseData.getHearingData());
        } else if (partyDetailsModel.getPartyRole().equalsIgnoreCase(RESPONDENT)) {
            boolean isSolicitor = isSolicitor(partyDetailsModel.getPartyID(), caseData.getRespondentsFL401());
            return getPreferredHearingChannel(-1, RESPONDENT, isSolicitor, caseData.getHearingData());
        }
        return null;
    }

    private void updatePreferredHearingChannel(PartyDetailsModel partyDetailsModel, CaseData caseData) {
        // if it's an organisation, it doesn't have individual details
        if (isNotEmpty(partyDetailsModel.getIndividualDetails())) {
            IndividualDetailsModel details = partyDetailsModel.getIndividualDetails();
            String preferredHearingChannel = null;
            if (caseData.getCaseTypeOfApplication().equalsIgnoreCase(C100)) {
                preferredHearingChannel = getPreferredHearingChannelC100(partyDetailsModel, caseData);
            } else if (caseData.getCaseTypeOfApplication().equalsIgnoreCase(FL401)) {
                preferredHearingChannel = getPreferredHearingChannelFL401(partyDetailsModel, caseData);
            }
            details.setPreferredHearingChannel(preferredHearingChannel);
        }
    }

    private List<AutomatedHearingCaseCategories> getCaseCategories() {
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

    private AutomatedHearingDetails getHearingDetails(CaseData caseData) {
        HearingData hearingData = caseData.getHearingData();
        DynamicListElement hearingType = hearingData.getHearingTypes().getValue();

        return AutomatedHearingDetails.automatedHearingDetailsWith()
            .autoListFlag(StringUtils.isEmpty(hearingData.getAdditionalHearingDetails())
                              && StringUtils.isEmpty(hearingData.getCustomDetails()))
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
            .hearingInWelshFlag(YES.equalsIgnoreCase(caseData.getAttendHearing().getIsWelshNeeded()))
            .hearingLocations(
                Collections.singletonList(
                    HearingLocation.hearingLocationWith()
                        .locationType(COURT)
                        .locationId(hearingData.getCourtList().getValueCode().split(":")[0]) //Extract court id without :
                        .build()))
            .facilitiesRequired(List.of())
            .listingComments(getListingComments(hearingData))
            .hearingRequester(null != hearingData.getHearingJudgePersonalCode()
                                  ? hearingData.getHearingJudgePersonalCode() : EMPTY)
            .privateHearingRequiredFlag(C100.equals(CaseUtils.getCaseTypeOfApplication(caseData)))
            .panelRequirements(new PanelRequirements()) //Need to revisit
            .leadJudgeContractType("")
            .hearingIsLinkedFlag(hearingData.getHearingListedLinkedCases().getValue() != null)
            .hearingChannels(List.of(hearingData.getHearingChannelsEnum().getId()))
            .build();
    }

    private String getListingComments(HearingData hearingData) {
        //PRL-7023 - set listing comments from free text field from hearing options 3/4
        if (Strings.isNotBlank(hearingData.getAdditionalHearingDetails())) {
            return hearingData.getAdditionalHearingDetails();
        } else if (Strings.isNotBlank(hearingData.getCustomDetails())) {
            return hearingData.getCustomDetails();
        }
        return null;
    }

    private String getHearingPriorityType(HearingPriorityTypeEnum hearingPriorityTypeEnum) {
        if (HearingPriorityTypeEnum.UrgentPriority.equals(hearingPriorityTypeEnum)) {
            return "Urgent";
        }
        return "Standard";
    }

    private HearingWindow hearingWindow(HearingData hearingData) {
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

    private int hearingDuration(String days, String hours, String minutes) {
        int daysInMin = 0;
        int hoursInMin = 0;
        int min = 0;
        if (Strings.isNotBlank(days)) {
            daysInMin = Integer.parseInt(days) * 360;
        }
        if (Strings.isNotBlank(hours)) {
            hoursInMin = Integer.parseInt(hours) * 60;
        }
        if (Strings.isNotBlank(minutes)) {
            min = Integer.parseInt(minutes);
            int remainder = Integer.parseInt(minutes) % 5;
            if (remainder != 0) {
                min = (Integer.parseInt(minutes) / 5) * 5 + 5;
            }
        }
        return daysInMin + hoursInMin + min;
    }

    private String dateOfHearing(@NotNull String firstDate, String hours, String minutes) {
        //Format hours & minutes to 2 digit format
        return String.format(
            "%sT%s:%s:00Z", firstDate,
            Strings.isNotBlank(hours) ? String.format("%02d", Integer.parseInt(hours)) : "00",
            Strings.isNotBlank(minutes) ? String.format("%02d", Integer.parseInt(minutes)) : "00"
        );
    }

    private int noOfPhysicalAttendees(String attendSameWayYesOrNo, HearingData hearingData,CaseData caseData) {
        int totalParticipants = 0;
        List<String> totalNoOfParties = new ArrayList<>();
        if (C100.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            totalNoOfParties.addAll(getParticipants(hearingData));
            //Local Authority and Cafcass
            totalNoOfParties.add(LOCAL_AUTHORITY);
            totalNoOfParties.add("Cafcass/Cafcass Cymru");
        } else if (FL401.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            totalNoOfParties.addAll(Lists.newArrayList(hearingData.getApplicantName(),
                                                       hearingData.getApplicantSolicitor(),
                                                       hearingData.getRespondentName(),
                                                       hearingData.getRespondentSolicitor()));
            //Local Authority and Cafcass
            totalNoOfParties.add(LOCAL_AUTHORITY);
        }
        totalNoOfParties.removeAll(Arrays.asList("", null));

        if (YesOrNo.YES.name().equalsIgnoreCase(attendSameWayYesOrNo)
            && HearingChannelsEnum.INTER.equals(hearingData.getHearingChannelsEnum())) {
            //All parties attending the hearing in the same way
            return totalNoOfParties.size();
        } else if (YesOrNo.NO.name().equalsIgnoreCase(attendSameWayYesOrNo)) {
            totalParticipants = totalNoOfParties.size();
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

    private List<DynamicList> getParticipantSelectedOptions(HearingData hearingData) {
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
        //Local Authority and Cafcass
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

    private List<String> getParticipants(HearingData hearingData) {
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

    private String getPreferredHearingChannel(int partyIndex,
                                                     String role,
                                                     boolean isSolicitor,
                                                     HearingData hearingData) {
        if (YesOrNo.YES.name().equalsIgnoreCase(hearingData.getAllPartiesAttendHearingSameWayYesOrNo())) {
            return hearingData.getHearingChannelsEnum().getId();
        } else {
            //Handle individual party/solicitor preferred hearing channel
            if (APPLICANT.equals(role)) {
                if (isSolicitor) {
                    return getApplicantSolicitorPreferredHearingChannel(partyIndex, hearingData);
                }
                return getApplicantPreferredHearingChannel(partyIndex, hearingData);
            } else if (RESPONDENT.equals(role)) {
                if (isSolicitor) {
                    return getRespondentSolicitorPreferredHearingChannel(partyIndex, hearingData);
                }
                return getRespondentPreferredHearingChannel(partyIndex, hearingData);
            }
        }
        return null;
    }

    private String getApplicantSolicitorPreferredHearingChannel(int partyIndex,
                                                                       HearingData hearingData) {
        return switch (partyIndex) {
            case -1 -> //FL401 applicant solicitor
                returnDynamicListValueCode(hearingData.getApplicantSolicitorHearingChannel());
            //C100 applicant solicitors
            case 0 ->
                returnDynamicListValueCode(hearingData.getHearingDataApplicantDetails().getApplicantSolicitorHearingChannel1());
            case 1 ->
                returnDynamicListValueCode(hearingData.getHearingDataApplicantDetails().getApplicantSolicitorHearingChannel2());
            case 2 ->
                returnDynamicListValueCode(hearingData.getHearingDataApplicantDetails().getApplicantSolicitorHearingChannel3());
            case 3 ->
                returnDynamicListValueCode(hearingData.getHearingDataApplicantDetails().getApplicantSolicitorHearingChannel4());
            case 4 ->
                returnDynamicListValueCode(hearingData.getHearingDataApplicantDetails().getApplicantSolicitorHearingChannel5());

            default -> null;
        };
    }

    private String getRespondentSolicitorPreferredHearingChannel(int partyIndex,
                                                                        HearingData hearingData) {
        return switch (partyIndex) {
            case -1 -> //FL401 respondent solicitor
                returnDynamicListValueCode(hearingData.getRespondentSolicitorHearingChannel());
            //C100 respondent solicitors
            case 0 ->
                returnDynamicListValueCode(hearingData.getHearingDataRespondentDetails().getRespondentSolicitorHearingChannel1());
            case 1 ->
                returnDynamicListValueCode(hearingData.getHearingDataRespondentDetails().getRespondentSolicitorHearingChannel2());
            case 2 ->
                returnDynamicListValueCode(hearingData.getHearingDataRespondentDetails().getRespondentSolicitorHearingChannel3());
            case 3 ->
                returnDynamicListValueCode(hearingData.getHearingDataRespondentDetails().getRespondentSolicitorHearingChannel4());
            case 4 ->
                returnDynamicListValueCode(hearingData.getHearingDataRespondentDetails().getRespondentSolicitorHearingChannel5());

            default -> null;
        };
    }

    private String getApplicantPreferredHearingChannel(int partyIndex,
                                                              HearingData hearingData) {
        return switch (partyIndex) {
            case -1 -> //FL401 applicant
                returnDynamicListValueCode(hearingData.getApplicantHearingChannel());
            //C100 applicants
            case 0 -> returnDynamicListValueCode(hearingData.getHearingDataApplicantDetails().getApplicantHearingChannel1());
            case 1 -> returnDynamicListValueCode(hearingData.getHearingDataApplicantDetails().getApplicantHearingChannel2());
            case 2 -> returnDynamicListValueCode(hearingData.getHearingDataApplicantDetails().getApplicantHearingChannel3());
            case 3 -> returnDynamicListValueCode(hearingData.getHearingDataApplicantDetails().getApplicantHearingChannel4());
            case 4 -> returnDynamicListValueCode(hearingData.getHearingDataApplicantDetails().getApplicantHearingChannel5());

            default -> null;
        };
    }

    private String getRespondentPreferredHearingChannel(int partyIndex,
                                                               HearingData hearingData) {
        return switch (partyIndex) {
            case -1 -> //FL401 respondent
                returnDynamicListValueCode(hearingData.getRespondentHearingChannel());
            //C100 respondents
            case 0 -> returnDynamicListValueCode(hearingData.getHearingDataRespondentDetails().getRespondentHearingChannel1());
            case 1 -> returnDynamicListValueCode(hearingData.getHearingDataRespondentDetails().getRespondentHearingChannel2());
            case 2 -> returnDynamicListValueCode(hearingData.getHearingDataRespondentDetails().getRespondentHearingChannel3());
            case 3 -> returnDynamicListValueCode(hearingData.getHearingDataRespondentDetails().getRespondentHearingChannel4());
            case 4 -> returnDynamicListValueCode(hearingData.getHearingDataRespondentDetails().getRespondentHearingChannel5());

            default -> null;
        };
    }

    private String returnDynamicListValueCode(DynamicList dynamicList) {
        return null != dynamicList && null != dynamicList.getValue()
            ? dynamicList.getValue().getCode() : null;
    }
}
