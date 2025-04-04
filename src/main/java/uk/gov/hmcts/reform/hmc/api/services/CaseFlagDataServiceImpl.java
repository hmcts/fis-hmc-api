package uk.gov.hmcts.reform.hmc.api.services;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.APPLICANT;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.EMPTY;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.EMPTY_STRING;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.LISTING_COMMENTS;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ONE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ORGANISATION;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.PF0002;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.PF0007;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.PF0013;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.PF0015;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.PF0018;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.PLUS_SIGN;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.RA;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.RA0042;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.RESPONDENT;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.SM;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.SM0002;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.TWO;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.mapper.FisHmcObjectMapper;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseDetailResponse;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseManagementLocation;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Element;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Flags;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Organisation;
import uk.gov.hmcts.reform.hmc.api.model.ccd.PartyDetails;
import uk.gov.hmcts.reform.hmc.api.model.ccd.flagdata.FlagDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseFlags;
import uk.gov.hmcts.reform.hmc.api.model.response.HearingLocation;
import uk.gov.hmcts.reform.hmc.api.model.response.IndividualDetailsModel;
import uk.gov.hmcts.reform.hmc.api.model.response.OrganisationDetailsModel;
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
     * deserialization ccd record for mapping the case flag data.
     *
     * @param caseDetails data which has caseDetails
     * @return CaseDetailResponse deserialization object
     * @throws IOException exception to type input/output
     */
    public CaseDetailResponse getCcdCaseData(CaseDetails caseDetails) throws IOException {
        ObjectMapper objectMapper = FisHmcObjectMapper.getObjectMapper();
        return objectMapper.convertValue(caseDetails, CaseDetailResponse.class);
    }

    /**
     * mapping the all parties flag data to ServiceHearingValues .
     *
     * @param serviceHearingValues data about hearings
     * @param caseDetails data about caseDetails
     * @throws IOException exception to input/output
     */
    public void setCaseFlagData(ServiceHearingValues serviceHearingValues, CaseDetails caseDetails)
            throws IOException {

        List<PartyFlagsModel> partiesFlagsModelList = new ArrayList<>();
        List<PartyDetailsModel> partyDetailsModelList = new ArrayList<>();
        CaseDetailResponse ccdResponse = getCcdCaseData(caseDetails);
        setBaseLocation(serviceHearingValues, ccdResponse);
        List<Element<PartyDetails>> applicantLst = ccdResponse.getCaseData().getApplicants();
        if (null != applicantLst) {
            addPartyFlagData(partiesFlagsModelList, partyDetailsModelList, applicantLst, APPLICANT);
        }

        List<Element<PartyDetails>> respondedLst = ccdResponse.getCaseData().getRespondents();
        if (null != respondedLst) {
            addPartyFlagData(
                    partiesFlagsModelList, partyDetailsModelList, respondedLst, RESPONDENT);
        }

        PartyDetails applicantsFL401 = ccdResponse.getCaseData().getApplicantsFL401();
        if (null != applicantsFL401) {
            addFL401PartyFlagData(
                    partiesFlagsModelList, partyDetailsModelList, applicantsFL401, APPLICANT);
        }

        PartyDetails respondentsFL401 = ccdResponse.getCaseData().getRespondentsFL401();
        if (null != respondentsFL401) {
            addFL401PartyFlagData(
                    partiesFlagsModelList, partyDetailsModelList, respondentsFL401, RESPONDENT);
        }
        if (!partiesFlagsModelList.isEmpty() || !partyDetailsModelList.isEmpty()) {
            CaseFlags caseFlags = CaseFlags.caseFlagsWith().flags(partiesFlagsModelList).build();
            serviceHearingValues.setCaseFlags(caseFlags);
            serviceHearingValues.setParties(partyDetailsModelList);
            serviceHearingValues.setCaseAdditionalSecurityFlag(
                    isCaseAdditionalSecurityFlag(partiesFlagsModelList));
            String listingComments = getListingComment(caseFlags.getFlags());
            serviceHearingValues.setListingComments(listingComments);
        }
    }

    /**
     * Hearing Base Location id setting for service values fromm ccd record.
     *
     * @param serviceHearingValues data about Hearing RequestValues
     * @param ccdResponse data about the ccd record
     */
    protected void setBaseLocation(
            ServiceHearingValues serviceHearingValues, CaseDetailResponse ccdResponse) {
        CaseManagementLocation caseManagementLocation =
                ccdResponse.getCaseData().getCaseManagementLocation();

        if (null != caseManagementLocation && null != caseManagementLocation.getBaseLocation()) {
            serviceHearingValues.setCaseManagementLocationCode(
                    caseManagementLocation.getBaseLocation());
            List<HearingLocation> locationList =
                    Arrays.asList(
                            HearingLocation.hearingLocationWith()
                                    .locationType(Constants.COURT)
                                    .locationId(caseManagementLocation.getBaseLocation())
                                    .build());
            serviceHearingValues.setHearingLocations(locationList);
        }
    }

    public static void addPartyFlagData(
            List<PartyFlagsModel> partiesFlagsModelList,
            List<PartyDetailsModel> partyDetailsModelList,
            List<Element<PartyDetails>> partyLst,
            String role) {

        UUID uuid;
        PartyDetails partyDetails;

        for (Element<PartyDetails> party : partyLst) {
            uuid = party.getId();
            partyDetails = party.getValue();
            List<PartyFlagsModel> curPartyFlagsModelList = getPartyFlagsModel(partyDetails, uuid);
            partiesFlagsModelList.addAll(curPartyFlagsModelList);
            preparePartyDetailsDTO(
                    partyDetailsModelList, partyDetails, uuid, role, curPartyFlagsModelList);
        }
    }

    public static void addFL401PartyFlagData(
            List<PartyFlagsModel> partiesFlagsModelList,
            List<PartyDetailsModel> partyDetailsModelList,
            PartyDetails partyDetails,
            String role) {
        if (null != partyDetails) {
            List<PartyFlagsModel> curPartyFlagsModelList =
                    getPartyFlagsModel(partyDetails, partyDetails.getPartyId());
            partiesFlagsModelList.addAll(curPartyFlagsModelList);
            preparePartyDetailsDTO(
                    partyDetailsModelList,
                    partyDetails,
                    partyDetails.getPartyId(),
                    role,
                    curPartyFlagsModelList);
        }
    }

    private static void preparePartyDetailsDTO(
            List<PartyDetailsModel> partyDetailsModelList,
            PartyDetails partyDetails,
            UUID uuid,
            String role,
            List<PartyFlagsModel> curPartyFlagsModelList) {
        String partyId = null;

        if (null != uuid) {
            partyId = uuid.toString();
        }
        List<Element<FlagDetail>> flagsDetailOfCurrParty = null;

        if (null != partyDetails.getPartyLevelFlag()) {
            flagsDetailOfCurrParty = partyDetails.getPartyLevelFlag().getDetails();
        }

        List<PartyFlagsModel> interpreterLangCodeList =
                getInterpreterLangCodes(curPartyFlagsModelList);

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

        IndividualDetailsModel individualDetailsModel;

        List<String> hearingChannelEmail =
                isNotBlank(partyDetails.getEmail())
                        ? Arrays.asList(partyDetails.getEmail())
                        : Arrays.asList();

        List<String> hearingChannelPhone =
                isNotBlank(partyDetails.getPhoneNumber())
                        ? Arrays.asList(partyDetails.getPhoneNumber())
                        : Arrays.asList();

        individualDetailsModel =
                IndividualDetailsModel.individualDetailsWith()
                        .firstName(partyDetails.getFirstName())
                        .lastName(partyDetails.getLastName())
                        .reasonableAdjustments(reasonableAdjustments)
                        .vulnerableFlag(isVulnerableFlag)
                        .vulnerabilityDetails(vulnerabilityDetails)
                        .hearingChannelEmail(hearingChannelEmail)
                        .hearingChannelPhone(hearingChannelPhone)
                        .interpreterLanguage(interpreterLanguageCode)
                        .relatedParties(Arrays.asList())
                        .build();
        PartyDetailsModel partyDetailsModel;
        partyDetailsModel =
                PartyDetailsModel.partyDetailsWith()
                        .partyID(partyId)
                        .partyName(
                                partyDetails.getFirstName()
                                        + EMPTY_STRING
                                        + partyDetails.getLastName())
                        .partyType(PartyType.IND)
                        .partyRole(role)
                        .individualDetails(individualDetailsModel)
                        .build();

        partyDetailsModelList.add(partyDetailsModel);

        Organisation org = partyDetails.getSolicitorOrg();

        /****** Organisation Party Details********/
        if (org != null && org.getOrganisationID() != null) {
            addPartyDetailsModelForOrg(
                    partyDetailsModelList, partyDetails, partyDetails.getSolicitorOrgUuid());
        }

        /******Solicitor Party Details*********/
        if (partyDetails.getRepresentativeFirstName() != null
                || partyDetails.getRepresentativeLastName() != null) {
            addPartyDetailsModelForSolicitor(
                    partyDetailsModelList, partyDetails, partyDetails.getSolicitorPartyId());
        }
    }

    protected static List<PartyFlagsModel> getInterpreterLangCodes(
            List<PartyFlagsModel> curPartyFlagsModelList) {
        return curPartyFlagsModelList.stream()
                .filter(
                        eachPartyFlag ->
                                eachPartyFlag.getFlagId().equals(RA0042)
                                        || eachPartyFlag.getFlagId().equals(PF0015))
                .distinct()
                .toList();
    }

    protected String getListingComment(List<PartyFlagsModel> flagsList) {

        boolean isListingCommentNeeded =
                flagsList.stream()
                                .map(PartyFlagsModel::getFlagId)
                                .filter(
                                        eachFlag ->
                                                eachFlag.equals(RA0042) || eachFlag.equals(PF0015))
                                .distinct()
                                .toList()
                                .size()
                        == TWO;

        return isListingCommentNeeded ? LISTING_COMMENTS : EMPTY;
    }

    private static List<PartyFlagsModel> getPartyFlagsModel(PartyDetails partyDetails, UUID uuid) {
        String partyId = null;
        if (null != uuid) {
            partyId = uuid.toString();
        }
        PartyFlagsModel partyFlagsModel;
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
                    partyFlagsModel =
                            PartyFlagsModel.partyFlagsModelWith()
                                    .partyId(partyId)
                                    .partyName(
                                            partyDetails.getFirstName()
                                                    + EMPTY_STRING
                                                    + partyDetails.getLastName())
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

    protected static List<String> getReasonableAdjustmentsByParty(
            List<Element<FlagDetail>> flagsDetailOfCurrParty) {

        return flagsDetailOfCurrParty.stream()
                .filter(
                        partyFlag ->
                                partyFlag.getValue().getFlagCode().startsWith(RA)
                                        || partyFlag.getValue().getFlagCode().startsWith(SM))
                .distinct()
                .map(partyFlag -> partyFlag.getValue().getFlagCode())
                .toList();
    }

    public static Boolean isCaseAdditionalSecurityFlag(List<PartyFlagsModel> partiesFlagsModelList) {

        return partiesFlagsModelList.stream()
                .anyMatch(partyFlag -> partyFlag.getFlagId().equals(PF0007));
    }

    protected static Boolean isVulnerableFlag(List<Element<FlagDetail>> flagsDetailOfCurrParty) {

        return flagsDetailOfCurrParty.stream()
                .anyMatch(
                        partyFlag ->
                                partyFlag.getValue().getFlagCode().equals(PF0002)
                                        || PF0013.equals(partyFlag.getValue().getFlagCode())
                                        || PF0018.equals(partyFlag.getValue().getFlagCode())
                                        || SM0002.equals(partyFlag.getValue().getFlagCode()));
    }

    protected static String getVulnerabilityDetails(List<Element<FlagDetail>> flagsDetailOfCurrParty) {

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

    protected static void addPartyDetailsModelForOrg(
            List<PartyDetailsModel> partyDetailsModelList, PartyDetails partyDetails, UUID uuid) {
        String partyId = null;
        if (uuid != null) {
            partyId = uuid.toString();
        }
        OrganisationDetailsModel organisationDetailsModel = null;
        PartyDetailsModel partyDetailsModelForOrg;
        organisationDetailsModel =
                OrganisationDetailsModel.organisationDetailsWith()
                        .name(partyDetails.getSolicitorOrg().getOrganisationName())
                        .cftOrganisationID(partyDetails.getSolicitorOrg().getOrganisationID())
                        .organisationType(PartyType.ORG.toString())
                        .build();

        partyDetailsModelForOrg =
                PartyDetailsModel.partyDetailsWith()
                        .partyID(partyId)
                        .partyName(partyDetails.getSolicitorOrg().getOrganisationName())
                        .partyType(PartyType.ORG)
                        .partyRole(ORGANISATION)
                        .organisationDetails(organisationDetailsModel)
                        .build();
        partyDetailsModelList.add(partyDetailsModelForOrg);
    }

    protected static void addPartyDetailsModelForSolicitor(
            List<PartyDetailsModel> partyDetailsModelList, PartyDetails partyDetails, UUID uuid) {

        String partyId = null;
        if (uuid != null) {
            partyId = uuid.toString();
        }

        IndividualDetailsModel individualDetailsModel;
        PartyDetailsModel partyDetailsModelForSol;

        List<String> hearingChannelEmail =
                isNotBlank(partyDetails.getSolicitorEmail())
                        ? Arrays.asList(partyDetails.getSolicitorEmail())
                        : Arrays.asList();

        if (isNotBlank(partyDetails.getRepresentativeFirstName())
            && isNotBlank(partyDetails.getRepresentativeLastName())) {
            individualDetailsModel =
                IndividualDetailsModel.individualDetailsWith()
                    .firstName(partyDetails.getRepresentativeFirstName())
                    .lastName(partyDetails.getRepresentativeLastName())
                    .hearingChannelEmail(hearingChannelEmail)
                    .build();

            partyDetailsModelForSol =
                PartyDetailsModel.partyDetailsWith()
                    .partyID(partyId)
                    .partyName(
                        partyDetails.getRepresentativeFirstName()
                            + EMPTY_STRING
                            + partyDetails.getRepresentativeLastName())
                    .partyType(PartyType.IND)
                    .partyRole(ORGANISATION)
                    .individualDetails(individualDetailsModel)
                    .build();
            partyDetailsModelList.add(partyDetailsModelForSol);
        }
    }
}
