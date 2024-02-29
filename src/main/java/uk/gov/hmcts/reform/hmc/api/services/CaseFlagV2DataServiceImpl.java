package uk.gov.hmcts.reform.hmc.api.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.api.enums.caseflags.PartyRole;
import uk.gov.hmcts.reform.hmc.api.mapper.FisHmcObjectMapper;
import uk.gov.hmcts.reform.hmc.api.model.ccd.CaseDetailResponse;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Element;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Flags;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Organisation;
import uk.gov.hmcts.reform.hmc.api.model.ccd.PartyDetails;
import uk.gov.hmcts.reform.hmc.api.model.ccd.flagdata.FlagDetail;
import uk.gov.hmcts.reform.hmc.api.model.response.CaseFlags;
import uk.gov.hmcts.reform.hmc.api.model.response.IndividualDetailsModel;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyDetailsModel;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyFlagsModel;
import uk.gov.hmcts.reform.hmc.api.model.response.PartyType;
import uk.gov.hmcts.reform.hmc.api.model.response.ServiceHearingValues;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseFlagV2DataServiceImpl extends CaseFlagDataServiceImpl {
    /**
     * mapping the all parties flag data to ServiceHearingValues .
     *
     * @param serviceHearingValues data about hearings
     * @param caseDetails          data about caseDetails
     * @throws IOException exception to input/output
     */
    public void setCaseFlagsV2Data(ServiceHearingValues serviceHearingValues, CaseDetails caseDetails) throws IOException {
        log.info("Service call happened to setCaseFlagsV2Data");
        CaseDetailResponse ccdResponse = getCcdCaseData(caseDetails);
        setBaseLocation(serviceHearingValues, ccdResponse);
        log.info("Base location is set");
        Map<String, Object> caseDataMap = caseDetails.getData();
        List<PartyFlagsModel> partiesFlagsModelList = new ArrayList<>();
        List<PartyDetailsModel> partyDetailsModelList = new ArrayList<>();
        if (C100.equalsIgnoreCase(ccdResponse.getCaseData().getCaseTypeOfApplication())) {
            findAndUpdateModelListsForC100(
                PartyRole.Representing.CAAPPLICANT,
                ccdResponse,
                caseDataMap,
                partiesFlagsModelList,
                partyDetailsModelList,
                APPLICANT
            );
            findAndUpdateModelListsForC100(
                PartyRole.Representing.CAAPPLICANTSOLICITOR,
                ccdResponse,
                caseDataMap,
                partiesFlagsModelList,
                partyDetailsModelList,
                APPLICANT
            );
            findAndUpdateModelListsForC100(
                PartyRole.Representing.CARESPONDENT,
                ccdResponse,
                caseDataMap,
                partiesFlagsModelList,
                partyDetailsModelList,
                RESPONDENT
            );
            findAndUpdateModelListsForC100(
                PartyRole.Representing.CARESPONDENTSOLICITOR,
                ccdResponse,
                caseDataMap,
                partiesFlagsModelList,
                partyDetailsModelList,
                RESPONDENT
            );
        } else if (FL401.equalsIgnoreCase(ccdResponse.getCaseData().getCaseTypeOfApplication())) {
            findAndUpdateModelListsForFL401(
                PartyRole.Representing.DAAPPLICANT,
                ccdResponse,
                caseDataMap,
                partiesFlagsModelList,
                partyDetailsModelList,
                APPLICANT
            );
            findAndUpdateModelListsForFL401(
                PartyRole.Representing.DAAPPLICANTSOLICITOR,
                ccdResponse,
                caseDataMap,
                partiesFlagsModelList,
                partyDetailsModelList,
                APPLICANT
            );
            findAndUpdateModelListsForFL401(
                PartyRole.Representing.DARESPONDENT,
                ccdResponse,
                caseDataMap,
                partiesFlagsModelList,
                partyDetailsModelList,
                RESPONDENT
            );
            findAndUpdateModelListsForFL401(
                PartyRole.Representing.DARESPONDENTSOLICITOR,
                ccdResponse,
                caseDataMap,
                partiesFlagsModelList,
                partyDetailsModelList,
                RESPONDENT
            );
        }

        if (!partiesFlagsModelList.isEmpty() || !partyDetailsModelList.isEmpty()) {
            CaseFlags caseFlags = CaseFlags.caseFlagsWith().flags(partiesFlagsModelList).build();
            serviceHearingValues.setCaseFlags(caseFlags);
            serviceHearingValues.setParties(partyDetailsModelList);
            serviceHearingValues.setCaseAdditionalSecurityFlag(isCaseAdditionalSecurityFlag(partiesFlagsModelList));
            String listingComments = getListingComment(caseFlags.getFlags());
            serviceHearingValues.setListingComments(listingComments);
        }
    }

    private void findAndUpdateModelListsForC100(PartyRole.Representing representing,
                                                CaseDetailResponse ccdResponse,
                                                Map<String, Object> caseDataMap,
                                                List<PartyFlagsModel> partiesFlagsModelList,
                                                List<PartyDetailsModel> partyDetailsModelList,
                                                String partyRole) {
        List<Element<PartyDetails>> partyDetailsListElements = representing.getCaTarget().apply(ccdResponse.getCaseData());

        int numElements = null != partyDetailsListElements ? partyDetailsListElements.size() : 0;
        List<PartyRole> partyRoles = PartyRole.matchingRoles(representing);
        for (int i = 0; i < partyRoles.size(); i++) {
            if (null != partyDetailsListElements) {
                Optional<Element<PartyDetails>> partyDetails = i < numElements ? Optional.of(partyDetailsListElements.get(
                    i)) : Optional.empty();
                if (partyDetails.isPresent()) {
                    List<Flags> partyFlagList = collateC100PartyFlags(representing, caseDataMap, i);
                    updateFlagContents(
                        partiesFlagsModelList,
                        partyDetailsModelList,
                        partyDetails.get(),
                        partyFlagList,
                        partyRole,
                        representing
                    );
                }
            }
        }
    }

    private List<Flags> collateC100PartyFlags(PartyRole.Representing representing, Map<String, Object> caseDataMap, int index) {
        List<Flags> partyFlagList = new ArrayList<>();
        String caseDataExternalField = String.format(representing.getCaseDataExternalField(), index + 1);
        findFlags(caseDataMap, caseDataExternalField).ifPresent(partyFlagList::add);
        String caseDataInternalField = String.format(representing.getCaseDataInternalField(), index + 1);
        findFlags(caseDataMap, caseDataInternalField).ifPresent(partyFlagList::add);
        return partyFlagList;
    }

    private void findAndUpdateModelListsForFL401(PartyRole.Representing representing,
                                                 CaseDetailResponse ccdResponse,
                                                 Map<String, Object> caseDataMap,
                                                 List<PartyFlagsModel> partiesFlagsModelList,
                                                 List<PartyDetailsModel> partyDetailsModelList,
                                                 String partyRole) {
        PartyDetails partyDetails = representing.getDaTarget().apply(ccdResponse.getCaseData());
        if (null != partyDetails) {
            List<Flags> partyFlagList = new ArrayList<>();
            String caseDataExternalField = representing.getCaseDataExternalField();
            findFlags(caseDataMap, caseDataExternalField).ifPresent(partyFlagList::add);
            String caseDataInternalField = representing.getCaseDataInternalField();
            findFlags(caseDataMap, caseDataInternalField).ifPresent(partyFlagList::add);
            updateFlagContents(
                partiesFlagsModelList,
                partyDetailsModelList,
                element(partyDetails.getPartyId(), partyDetails),
                partyFlagList,
                partyRole,
                representing
            );
        }
    }

    private Optional<Flags> findFlags(Map<String, Object> caseDataMap, String caseDataExternalField) {
        ObjectMapper objectMapper = FisHmcObjectMapper.getObjectMapper();
        if (caseDataMap.containsKey(caseDataExternalField)) {
            return Optional.ofNullable(objectMapper.convertValue(caseDataMap.get(caseDataExternalField), Flags.class));
        }
        return Optional.empty();
    }

    private void updateFlagContents(List<PartyFlagsModel> partiesFlagsModelList,
                                    List<PartyDetailsModel> partyDetailsModelList,
                                    Element<PartyDetails> partyDetailsElement,
                                    List<Flags> partyFlagList,
                                    String role,
                                    PartyRole.Representing representing) {
        List<PartyFlagsModel> curPartyFlagsModelList = new ArrayList<>();
        if (!partyFlagList.isEmpty()) {
            curPartyFlagsModelList = getPartyFlagsModel(partyDetailsElement, partyFlagList);
            partiesFlagsModelList.addAll(curPartyFlagsModelList);
        }

        preparePartyDetailsDTO(
            partyDetailsModelList,
            curPartyFlagsModelList,
            partyDetailsElement,
            partyFlagList,
            role,
            representing
        );
    }

    private List<PartyFlagsModel> getPartyFlagsModel(Element<PartyDetails> partyDetailsElement, List<Flags> partyFlagList) {
        List<PartyFlagsModel> partyFlagsModelList = new ArrayList<>();
        String partyId = partyDetailsElement.getId() != null ? partyDetailsElement.getId().toString() : null;

        for (Flags flag : partyFlagList) {
            List<Element<FlagDetail>> detailsList = flag.getDetails();

            if (detailsList != null) {
                for (Element<FlagDetail> flagDetailElement : detailsList) {
                    FlagDetail flagDetail = flagDetailElement.getValue();
                    if (null != flagDetail) {
                        PartyFlagsModel partyFlagsModel = PartyFlagsModel.partyFlagsModelWith().partyId(partyId).partyName(
                            flag.getPartyName()).flagId(flagDetail.getFlagCode()).flagStatus(flagDetail.getStatus()).flagParentId(
                            EMPTY).languageCode(flagDetail.getSubTypeKey()).flagDescription(flagDetail.getFlagComment()).build();
                        partyFlagsModelList.add(partyFlagsModel);
                    }
                }
            }
        }
        return partyFlagsModelList;
    }

    private void preparePartyDetailsDTO(List<PartyDetailsModel> partyDetailsModelList,
                                        List<PartyFlagsModel> partyFlagsModelList,
                                        Element<PartyDetails> partyDetailsElement,
                                        List<Flags> partyFlagList,
                                        String role,
                                        PartyRole.Representing representing) {

        PartyDetails partyDetails = partyDetailsElement.getValue();
        if (representing.equals(PartyRole.Representing.CAAPPLICANT)
            || representing.equals(PartyRole.Representing.CARESPONDENT)
            || representing.equals(PartyRole.Representing.DAAPPLICANT)
            || representing.equals(PartyRole.Representing.DARESPONDENT)) {
            generatePartyDetails(
                partyDetailsModelList,
                partyFlagsModelList,
                partyDetailsElement,
                partyFlagList,
                role,
                partyDetails
            );
        }

        if ((representing.equals(PartyRole.Representing.CAAPPLICANTSOLICITOR)
            || representing.equals(PartyRole.Representing.CARESPONDENTSOLICITOR)
            || representing.equals(PartyRole.Representing.DAAPPLICANTSOLICITOR)
            || representing.equals(PartyRole.Representing.DARESPONDENTSOLICITOR))
            && (StringUtils.isNotEmpty(partyDetails.getRepresentativeFirstName())
            && StringUtils.isNotEmpty(partyDetails.getRepresentativeLastName()))) {
            Organisation org = partyDetails.getSolicitorOrg();

            if (org != null && org.getOrganisationID() != null) {
                addPartyDetailsModelForOrg(partyDetailsModelList, partyDetails, partyDetails.getSolicitorOrgUuid());
            }
            generateRepresentativeDetails(
                partyDetailsModelList,
                partyFlagsModelList,
                partyFlagList,
                partyDetails
            );
        }
    }

    private void generateRepresentativeDetails(List<PartyDetailsModel> partyDetailsModelList, List<PartyFlagsModel> partyFlagsModelList, List<Flags> partyFlagList, PartyDetails partyDetails) {
        List<PartyFlagsModel> interpreterLangCodeList = getInterpreterLangCodes(partyFlagsModelList);

        String interpreterLanguageCode = EMPTY;
        if (interpreterLangCodeList.size() == ONE) {
            interpreterLanguageCode = (interpreterLangCodeList.get(0).getLanguageCode() != null) ? interpreterLangCodeList.get(
                0).getLanguageCode() : EMPTY;
        }
        Boolean isVulnerableFlag = false;
        String vulnerabilityDetails = "";
        List<String> reasonableAdjustments = null;

        List<Element<FlagDetail>> flagsDetailsList = new ArrayList<>();
        for (Flags flags : partyFlagList) {
            flagsDetailsList.addAll(flags.getDetails());
        }

        if (!flagsDetailsList.isEmpty()) {
            isVulnerableFlag = isVulnerableFlag(flagsDetailsList);
            vulnerabilityDetails = getVulnerabilityDetails(flagsDetailsList);
            reasonableAdjustments = getReasonableAdjustmentsByParty(flagsDetailsList);
        }
        String partyId = null;
        if (partyDetails.getSolicitorPartyId() != null) {
            partyId = partyDetails.getSolicitorPartyId().toString();
        }
        List<String> hearingChannelEmail = !isBlank(partyDetails.getSolicitorEmail())
            ? Arrays.asList(partyDetails.getSolicitorEmail()) : Arrays.asList();

        List<String> hearingChannelPhone = !isBlank(partyDetails.getSolicitorTelephone())
            ? Arrays.asList(partyDetails.getSolicitorTelephone()) : Arrays.asList();

        IndividualDetailsModel individualDetailsModel = IndividualDetailsModel.individualDetailsWith()
            .firstName(partyDetails.getRepresentativeFirstName())
            .lastName(partyDetails.getRepresentativeLastName())
            .reasonableAdjustments(reasonableAdjustments)
            .vulnerableFlag(isVulnerableFlag)
            .vulnerabilityDetails(vulnerabilityDetails)
            .hearingChannelEmail(hearingChannelEmail)
            .hearingChannelPhone(hearingChannelPhone)
            .interpreterLanguage(interpreterLanguageCode)
            .relatedParties(Arrays.asList())
            .build();

        PartyDetailsModel partyDetailsModelForSol =
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

    private void generatePartyDetails(List<PartyDetailsModel> partyDetailsModelList, List<PartyFlagsModel> partyFlagsModelList, Element<PartyDetails> partyDetailsElement, List<Flags> partyFlagList, String role, PartyDetails partyDetails) {
        List<PartyFlagsModel> interpreterLangCodeList = getInterpreterLangCodes(partyFlagsModelList);

        String interpreterLanguageCode = EMPTY;
        if (interpreterLangCodeList.size() == ONE) {
            interpreterLanguageCode = (interpreterLangCodeList.get(0).getLanguageCode() != null) ? interpreterLangCodeList.get(
                0).getLanguageCode() : EMPTY;
        }
        Boolean isVulnerableFlag = false;
        String vulnerabilityDetails = "";
        List<String> reasonableAdjustments = null;

        List<Element<FlagDetail>> flagsDetailsList = new ArrayList<>();
        for (Flags flags : partyFlagList) {
            flagsDetailsList.addAll(flags.getDetails());
        }

        if (!flagsDetailsList.isEmpty()) {
            isVulnerableFlag = isVulnerableFlag(flagsDetailsList);
            vulnerabilityDetails = getVulnerabilityDetails(flagsDetailsList);
            reasonableAdjustments = getReasonableAdjustmentsByParty(flagsDetailsList);
        }
        List<String> hearingChannelEmail = !isBlank(partyDetails.getEmail())
            ? Arrays.asList(partyDetails.getEmail()) : Arrays.asList();

        List<String> hearingChannelPhone = !isBlank(partyDetails.getPhoneNumber())
            ? Arrays.asList(partyDetails.getPhoneNumber()) : Arrays.asList();
        IndividualDetailsModel individualDetailsModel =
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

        String partyId = partyDetailsElement.getId() != null ? partyDetailsElement.getId().toString() : null;

        PartyDetailsModel partyDetailsModel = PartyDetailsModel.partyDetailsWith().partyID(partyId).partyName(
                partyDetails.getFirstName() + EMPTY_STRING + partyDetails.getLastName())
            .partyType(PartyType.IND).partyRole(role)
            .individualDetails(individualDetailsModel).build();

        partyDetailsModelList.add(partyDetailsModel);

    }

    public static <T> Element<T> element(UUID id, T element) {
        return Element.<T>builder().id(id).value(element).build();
    }
}
