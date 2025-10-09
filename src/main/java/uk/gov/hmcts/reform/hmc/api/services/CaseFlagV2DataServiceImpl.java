package uk.gov.hmcts.reform.hmc.api.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.hmc.api.utils.CaseUtils.formatPhoneNumber;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.APPLICANT;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.C100;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.EMPTY;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.EMPTY_STRING;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.FL401;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ONE;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.ORGANISATION;
import static uk.gov.hmcts.reform.hmc.api.utils.Constants.RESPONDENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseFlagV2DataServiceImpl extends CaseFlagDataServiceImpl {

    @Value("${hearing.specialCharacters}")
    private String specialCharacters;

    /**
     * Mapping ACTIVE party flags to ServiceHearingValues .
     *
     * @param serviceHearingValues data about hearings
     * @param caseDetails          data about caseDetails
     * @throws IOException exception to input/output
     */
    public void setCaseFlagsV2Data(ServiceHearingValues serviceHearingValues, CaseDetails caseDetails)
        throws IOException {
        log.info("Service call happened to setCaseFlagsV2Data");
        CaseDetailResponse ccdResponse = getCcdCaseData(caseDetails);
        setBaseLocation(serviceHearingValues, ccdResponse);
        log.info("Base location is set");

        Map<String, Object> caseDataMap = caseDetails.getData();
        List<PartyFlagsModel> partiesFlagsModelList = new ArrayList<>();
        List<PartyDetailsModel> partyDetailsModelList = new ArrayList<>();

        String caseType = ccdResponse.getCaseData().getCaseTypeOfApplication();

        if (C100.equalsIgnoreCase(caseType)) {
            // Applicants + their solicitors
            findAndUpdateModelListsForC100(
                PartyRole.Representing.CAAPPLICANT, ccdResponse, caseDataMap,
                partiesFlagsModelList, partyDetailsModelList, APPLICANT
            );
            findAndUpdateModelListsForC100(
                PartyRole.Representing.CAAPPLICANTSOLICITOR, ccdResponse, caseDataMap,
                partiesFlagsModelList, partyDetailsModelList, APPLICANT
            );

            // Respondents + their solicitors
            findAndUpdateModelListsForC100(
                PartyRole.Representing.CARESPONDENT, ccdResponse, caseDataMap,
                partiesFlagsModelList, partyDetailsModelList, RESPONDENT
            );
            findAndUpdateModelListsForC100(
                PartyRole.Representing.CARESPONDENTSOLICITOR, ccdResponse, caseDataMap,
                partiesFlagsModelList, partyDetailsModelList, RESPONDENT
            );

        } else if (FL401.equalsIgnoreCase(caseType)) {
            findAndUpdateModelListsForFL401(
                PartyRole.Representing.DAAPPLICANT, ccdResponse, caseDataMap,
                partiesFlagsModelList, partyDetailsModelList, APPLICANT
            );
            findAndUpdateModelListsForFL401(
                PartyRole.Representing.DAAPPLICANTSOLICITOR, ccdResponse, caseDataMap,
                partiesFlagsModelList, partyDetailsModelList, APPLICANT
            );

            findAndUpdateModelListsForFL401(
                PartyRole.Representing.DARESPONDENT, ccdResponse, caseDataMap,
                partiesFlagsModelList, partyDetailsModelList, RESPONDENT
            );
            findAndUpdateModelListsForFL401(
                PartyRole.Representing.DARESPONDENTSOLICITOR, ccdResponse, caseDataMap,
                partiesFlagsModelList, partyDetailsModelList, RESPONDENT
            );
        }

        if (!partiesFlagsModelList.isEmpty() || !partyDetailsModelList.isEmpty()) {
            CaseFlags caseFlags = CaseFlags.caseFlagsWith().flags(partiesFlagsModelList).build();
            serviceHearingValues.setCaseFlags(caseFlags);
            serviceHearingValues.setParties(partyDetailsModelList);
            serviceHearingValues.setCaseAdditionalSecurityFlag(isCaseAdditionalSecurityFlag(partiesFlagsModelList));
            serviceHearingValues.setListingComments(getListingComment(caseFlags.getFlags()));
        }
    }

    // ACTIVE flags helper
    private List<Element<FlagDetail>> activeDetails(Flags flags) {
        if (flags == null || flags.getDetails() == null) {
            return Collections.emptyList();
        }
        return flags.getDetails().stream()
            .filter(e -> {
                FlagDetail flatDetail = e.getValue();
                return flatDetail != null && "Active".equalsIgnoreCase(flatDetail.getStatus());
            })
            .toList();
    }

    private void findAndUpdateModelListsForC100(PartyRole.Representing representing,
                                                CaseDetailResponse ccdResponse,
                                                Map<String, Object> caseDataMap,
                                                List<PartyFlagsModel> partiesFlagsModelList,
                                                List<PartyDetailsModel> partyDetailsModelList,
                                                String partyRole) {

        List<Element<PartyDetails>> partyDetailsListElements =
            representing.getCaTarget().apply(ccdResponse.getCaseData());
        if (partyDetailsListElements == null || partyDetailsListElements.isEmpty()) {
            return;
        }

        final List<Flags> flagBuffer = new ArrayList<>(2);

        for (int i = 0; i < partyDetailsListElements.size(); i++) {
            flagBuffer.clear();
            String externalKey = String.format(representing.getCaseDataExternalField(), i + 1);
            String internalKey = String.format(representing.getCaseDataInternalField(), i + 1);
            findFlags(caseDataMap, externalKey).ifPresent(flagBuffer::add);
            findFlags(caseDataMap, internalKey).ifPresent(flagBuffer::add);

            List<Flags> partyFlagList = flagBuffer.isEmpty() ? Collections.emptyList() : List.copyOf(flagBuffer);

            updateFlagContents(
                partiesFlagsModelList,
                partyDetailsModelList,
                partyDetailsListElements.get(i),
                partyFlagList,
                partyRole,
                representing
            );
        }
    }


    private void findAndUpdateModelListsForFL401(PartyRole.Representing representing,
                                                 CaseDetailResponse ccdResponse,
                                                 Map<String, Object> caseDataMap,
                                                 List<PartyFlagsModel> partiesFlagsModelList,
                                                 List<PartyDetailsModel> partyDetailsModelList,
                                                 String partyRole) {

        PartyDetails partyDetails = representing.getDaTarget().apply(ccdResponse.getCaseData());
        if (partyDetails == null) {
            return;
        }

        List<Flags> partyFlagList = new ArrayList<>();
        String externalKey = representing.getCaseDataExternalField();
        String internalKey = representing.getCaseDataInternalField();
        findFlags(caseDataMap, externalKey).ifPresent(partyFlagList::add);
        findFlags(caseDataMap, internalKey).ifPresent(partyFlagList::add);

        updateFlagContents(
            partiesFlagsModelList,
            partyDetailsModelList,
            element(partyDetails.getPartyId(), partyDetails),
            partyFlagList,
            partyRole,
            representing
        );
    }

    private Optional<Flags> findFlags(Map<String, Object> caseDataMap, String caseDataField) {
        ObjectMapper objectMapper = FisHmcObjectMapper.getObjectMapper();
        if (caseDataMap.containsKey(caseDataField)) {
            return Optional.ofNullable(objectMapper.convertValue(caseDataMap.get(caseDataField), Flags.class));
        }
        return Optional.empty();
    }

    private void updateFlagContents(List<PartyFlagsModel> partiesFlagsModelList,
                                    List<PartyDetailsModel> partyDetailsModelList,
                                    Element<PartyDetails> partyDetailsElement,
                                    List<Flags> partyFlagList,
                                    String role,
                                    PartyRole.Representing representing) {

        // ACTIVE flags to List Assist only
        List<PartyFlagsModel> curPartyFlagsModelList = getPartyFlagsModel(partyDetailsElement, partyFlagList);
        if (!curPartyFlagsModelList.isEmpty()) {
            partiesFlagsModelList.addAll(curPartyFlagsModelList);
        }

        // Build PartyDetailsModel using the same ACTIVE detail set for derivations
        preparePartyDetailsDTO(
            partyDetailsModelList,
            curPartyFlagsModelList,
            partyDetailsElement,
            partyFlagList,
            role,
            representing
        );
    }

    /**
     * Build PartyFlagsModel rows for ACTIVE details only.
     */
    private List<PartyFlagsModel> getPartyFlagsModel(Element<PartyDetails> partyDetailsElement,
                                                     List<Flags> partyFlagList) {
        List<PartyFlagsModel> partyFlagsModelList = new ArrayList<>();
        String partyId = partyDetailsElement.getId() != null ? partyDetailsElement.getId().toString() : null;

        for (Flags flag : partyFlagList) {
            for (Element<FlagDetail> e : activeDetails(flag)) {
                FlagDetail flagDetail = e.getValue();
                partyFlagsModelList.add(
                    PartyFlagsModel.partyFlagsModelWith()
                        .partyId(partyId)
                        .partyName(flag.getPartyName())
                        .flagId(flagDetail.getFlagCode())
                        .flagStatus(flagDetail.getStatus())
                        .flagParentId(EMPTY)
                        .languageCode(flagDetail.getSubTypeKey())
                        .flagDescription(flagDetail.getFlagComment())
                        .build()
                );
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
            && StringUtils.isNotEmpty(partyDetails.getRepresentativeFirstName())
            && StringUtils.isNotEmpty(partyDetails.getRepresentativeLastName())) {

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

    private void generateRepresentativeDetails(List<PartyDetailsModel> partyDetailsModelList,
                                               List<PartyFlagsModel> partyFlagsModelList,
                                               List<Flags> partyFlagList,
                                               PartyDetails partyDetails) {
        List<PartyFlagsModel> interpreterLangCodeList = getInterpreterLangCodes(partyFlagsModelList);

        String interpreterLanguageCode = EMPTY;
        if (interpreterLangCodeList.size() == ONE) {
            interpreterLanguageCode = (interpreterLangCodeList.get(0).getLanguageCode() != null)
                ? interpreterLangCodeList.get(0).getLanguageCode() : EMPTY;
        }

        Boolean isVulnerableFlag = false;
        String vulnerabilityDetails = "";
        List<String> reasonableAdjustments = null;

        // Use ACTIVE detail set for derivations (null-safe)
        List<Element<FlagDetail>> activeDetailsList = new ArrayList<>();
        for (Flags flags : partyFlagList) {
            activeDetailsList.addAll(activeDetails(flags));
        }

        if (!activeDetailsList.isEmpty()) {
            isVulnerableFlag = isVulnerableFlag(activeDetailsList);
            vulnerabilityDetails = getVulnerabilityDetails(activeDetailsList);
            reasonableAdjustments = getReasonableAdjustmentsByParty(activeDetailsList);
        }

        String partyId =
            partyDetails.getSolicitorPartyId() != null ? partyDetails.getSolicitorPartyId().toString() : null;

        List<String> hearingChannelEmail = !isBlank(partyDetails.getSolicitorEmail())
            ? Arrays.asList(partyDetails.getSolicitorEmail()) : Arrays.asList();
        List<String> hearingChannelPhone = !isBlank(partyDetails.getSolicitorTelephone())
            ? Arrays.asList(formatPhoneNumber(partyDetails.getSolicitorTelephone(), specialCharacters)) : Arrays.asList();

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

        PartyDetailsModel partyDetailsModelForSol = PartyDetailsModel.partyDetailsWith()
            .partyID(partyId)
            .partyName(
                partyDetails.getRepresentativeFirstName() + EMPTY_STRING + partyDetails.getRepresentativeLastName())
            .partyType(PartyType.IND)
            .partyRole(ORGANISATION)
            .individualDetails(individualDetailsModel)
            .build();

        partyDetailsModelList.add(partyDetailsModelForSol);
    }

    private void generatePartyDetails(List<PartyDetailsModel> partyDetailsModelList,
                                      List<PartyFlagsModel> partyFlagsModelList,
                                      Element<PartyDetails> partyDetailsElement,
                                      List<Flags> partyFlagList,
                                      String role,
                                      PartyDetails partyDetails) {

        List<PartyFlagsModel> interpreterLangCodeList = getInterpreterLangCodes(partyFlagsModelList);

        String interpreterLanguageCode = EMPTY;
        if (interpreterLangCodeList.size() == ONE) {
            interpreterLanguageCode = (interpreterLangCodeList.get(0).getLanguageCode() != null)
                ? interpreterLangCodeList.get(0).getLanguageCode() : EMPTY;
        }

        Boolean isVulnerableFlag = false;
        String vulnerabilityDetails = "";
        List<String> reasonableAdjustments = null;

        // ACTIVE-only detail set
        List<Element<FlagDetail>> activeDetailsList = new ArrayList<>();
        for (Flags flags : partyFlagList) {
            activeDetailsList.addAll(activeDetails(flags));
        }

        if (!activeDetailsList.isEmpty()) {
            isVulnerableFlag = isVulnerableFlag(activeDetailsList);
            vulnerabilityDetails = getVulnerabilityDetails(activeDetailsList);
            reasonableAdjustments = getReasonableAdjustmentsByParty(activeDetailsList);
        }

        List<String> hearingChannelEmail = !isBlank(partyDetails.getEmail())
            ? Arrays.asList(partyDetails.getEmail()) : Arrays.asList();

        List<String> hearingChannelPhone = !isBlank(partyDetails.getPhoneNumber())
            ? Arrays.asList(formatPhoneNumber(partyDetails.getPhoneNumber(), specialCharacters)) : Arrays.asList();

        IndividualDetailsModel individualDetailsModel = IndividualDetailsModel.individualDetailsWith()
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

        PartyDetailsModel partyDetailsModel = PartyDetailsModel.partyDetailsWith()
            .partyID(partyId)
            .partyName(partyDetails.getFirstName() + EMPTY_STRING + partyDetails.getLastName())
            .partyType(PartyType.IND)
            .partyRole(role)
            .individualDetails(individualDetailsModel)
            .build();

        partyDetailsModelList.add(partyDetailsModel);

    }

    public static <T> Element<T> element(UUID id, T element) {
        return Element.<T>builder().id(id).value(element).build();
    }

}
