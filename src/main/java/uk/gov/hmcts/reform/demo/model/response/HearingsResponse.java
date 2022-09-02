package uk.gov.hmcts.reform.demo.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class HearingsResponse {
    @JsonProperty("caseDeepLink")
    public String getCaseDeepLink() {
        return this.caseDeepLink;
    }

    public void setCaseDeepLink(String caseDeepLink) {
        this.caseDeepLink = caseDeepLink;
    }

    String caseDeepLink;

    @JsonProperty("caseManagementLocationCode")
    public String getCaseManagementLocationCode() {
        return this.caseManagementLocationCode;
    }

    public void setCaseManagementLocationCode(String caseManagementLocationCode) {
        this.caseManagementLocationCode = caseManagementLocationCode;
    }

    String caseManagementLocationCode;

    @JsonProperty("externalCaseReference")
    public Object getExternalCaseReference() {
        return this.externalCaseReference;
    }

    public void setExternalCaseReference(Object externalCaseReference) {
        this.externalCaseReference = externalCaseReference;
    }

    Object externalCaseReference;

    @JsonProperty("hearingChannels")
    public ArrayList<String> getHearingChannels() {
        return this.hearingChannels;
    }

    public void setHearingChannels(ArrayList<String> hearingChannels) {
        this.hearingChannels = hearingChannels;
    }

    ArrayList<String> hearingChannels;

    @JsonProperty("hmctsInternalCaseName")
    public String getHmctsInternalCaseName() {
        return this.hmctsInternalCaseName;
    }

    public void setHmctsInternalCaseName(String hmctsInternalCaseName) {
        this.hmctsInternalCaseName = hmctsInternalCaseName;
    }

    String hmctsInternalCaseName;

    @JsonProperty("publicCaseName")
    public String getPublicCaseName() {
        return this.publicCaseName;
    }

    public void setPublicCaseName(String publicCaseName) {
        this.publicCaseName = publicCaseName;
    }

    String publicCaseName;

    @JsonProperty("autoListFlag")
    public boolean getAutoListFlag() {
        return this.autoListFlag;
    }

    public void setAutoListFlag(boolean autoListFlag) {
        this.autoListFlag = autoListFlag;
    }

    boolean autoListFlag;

    @JsonProperty("hearingType")
    public String getHearingType() {
        return this.hearingType;
    }

    public void setHearingType(String hearingType) {
        this.hearingType = hearingType;
    }

    String hearingType;

    @JsonProperty("caseType")
    public String getCaseType() {
        return this.caseType;
    }

    public void setCaseType(String caseType) {
        this.caseType = caseType;
    }

    String caseType;

    @JsonProperty("caseCategories")
    public ArrayList<CaseCategory> getCaseCategories() {
        return this.caseCategories;
    }

    public void setCaseCategories(ArrayList<CaseCategory> caseCategories) {
        this.caseCategories = caseCategories;
    }

    ArrayList<CaseCategory> caseCategories;

    @JsonProperty("duration")
    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    int duration;

    @JsonProperty("hearingPriorityType")
    public String getHearingPriorityType() {
        return this.hearingPriorityType;
    }

    public void setHearingPriorityType(String hearingPriorityType) {
        this.hearingPriorityType = hearingPriorityType;
    }

    String hearingPriorityType;

    @JsonProperty("numberOfPhysicalAttendees")
    public int getNumberOfPhysicalAttendees() {
        return this.numberOfPhysicalAttendees;
    }

    public void setNumberOfPhysicalAttendees(int numberOfPhysicalAttendees) {
        this.numberOfPhysicalAttendees = numberOfPhysicalAttendees;
    }

    int numberOfPhysicalAttendees;

    @JsonProperty("hearingInWelshFlag")
    public boolean getHearingInWelshFlag() {
        return this.hearingInWelshFlag;
    }

    public void setHearingInWelshFlag(boolean hearingInWelshFlag) {
        this.hearingInWelshFlag = hearingInWelshFlag;
    }

    boolean hearingInWelshFlag;

    @JsonProperty("caseAdditionalSecurityFlag")
    public boolean getCaseAdditionalSecurityFlag() {
        return this.caseAdditionalSecurityFlag;
    }

    public void setCaseAdditionalSecurityFlag(boolean caseAdditionalSecurityFlag) {
        this.caseAdditionalSecurityFlag = caseAdditionalSecurityFlag;
    }

    boolean caseAdditionalSecurityFlag;

    @JsonProperty("facilitiesRequired")
    public ArrayList<Object> getFacilitiesRequired() {
        return this.facilitiesRequired;
    }

    public void setFacilitiesRequired(ArrayList<Object> facilitiesRequired) {
        this.facilitiesRequired = facilitiesRequired;
    }

    ArrayList<Object> facilitiesRequired;

    @JsonProperty("listingComments")
    public Object getListingComments() {
        return this.listingComments;
    }

    public void setListingComments(Object listingComments) {
        this.listingComments = listingComments;
    }

    Object listingComments;

    @JsonProperty("hearingRequester")
    public Object getHearingRequester() {
        return this.hearingRequester;
    }

    public void setHearingRequester(Object hearingRequester) {
        this.hearingRequester = hearingRequester;
    }

    Object hearingRequester;

    @JsonProperty("privateHearingRequiredFlag")
    public boolean getPrivateHearingRequiredFlag() {
        return this.privateHearingRequiredFlag;
    }

    public void setPrivateHearingRequiredFlag(boolean privateHearingRequiredFlag) {
        this.privateHearingRequiredFlag = privateHearingRequiredFlag;
    }

    boolean privateHearingRequiredFlag;

    @JsonProperty("leadJudgeContractType")
    public Object getLeadJudgeContractType() {
        return this.leadJudgeContractType;
    }

    public void setLeadJudgeContractType(Object leadJudgeContractType) {
        this.leadJudgeContractType = leadJudgeContractType;
    }

    Object leadJudgeContractType;

    @JsonProperty("hearingIsLinkedFlag")
    public boolean getHearingIsLinkedFlag() {
        return this.hearingIsLinkedFlag;
    }

    public void setHearingIsLinkedFlag(boolean hearingIsLinkedFlag) {
        this.hearingIsLinkedFlag = hearingIsLinkedFlag;
    }

    boolean hearingIsLinkedFlag;

    @JsonProperty("hmctsServiceID")
    public String getHmctsServiceID() {
        return this.hmctsServiceID;
    }

    public void setHmctsServiceID(String hmctsServiceID) {
        this.hmctsServiceID = hmctsServiceID;
    }

    String hmctsServiceID;

    @JsonProperty("caseInterpreterRequiredFlag")
    public boolean getCaseInterpreterRequiredFlag() {
        return this.caseInterpreterRequiredFlag;
    }

    public void setCaseInterpreterRequiredFlag(boolean caseInterpreterRequiredFlag) {
        this.caseInterpreterRequiredFlag = caseInterpreterRequiredFlag;
    }

    boolean caseInterpreterRequiredFlag;

    @JsonProperty("caserestrictedFlag")
    public boolean getCaserestrictedFlag() {
        return this.caserestrictedFlag;
    }

    public void setCaserestrictedFlag(boolean caserestrictedFlag) {
        this.caserestrictedFlag = caserestrictedFlag;
    }

    boolean caserestrictedFlag;

    @JsonProperty("caseSLAStartDate")
    public String getCaseSLAStartDate() {
        return this.caseSLAStartDate;
    }

    public void setCaseSLAStartDate(String caseSLAStartDate) {
        this.caseSLAStartDate = caseSLAStartDate;
    }

    String caseSLAStartDate;

    @JsonProperty("hearingWindow")
    public HearingWindow getHearingWindow() {
        return this.hearingWindow;
    }

    public void setHearingWindow(HearingWindow hearingWindow) {
        this.hearingWindow = hearingWindow;
    }

    HearingWindow hearingWindow;

    @JsonProperty("hearingLocations")
    public ArrayList<HearingLocation> getHearingLocations() {
        return this.hearingLocations;
    }

    public void setHearingLocations(ArrayList<HearingLocation> hearingLocations) {
        this.hearingLocations = hearingLocations;
    }

    ArrayList<HearingLocation> hearingLocations;

    @JsonProperty("panelRequirements")
    public Object getPanelRequirements() {
        return this.panelRequirements;
    }

    public void setPanelRequirements(Object panelRequirements) {
        this.panelRequirements = panelRequirements;
    }

    Object panelRequirements;

    @JsonProperty("judiciary")
    public Judiciary getJudiciary() {
        return this.judiciary;
    }

    public void setJudiciary(Judiciary judiciary) {
        this.judiciary = judiciary;
    }

    Judiciary judiciary;

    @JsonProperty("parties")
    public ArrayList<Party> getParties() {
        return this.parties;
    }

    public void setParties(ArrayList<Party> parties) {
        this.parties = parties;
    }

    ArrayList<Party> parties;

    @JsonProperty("caseFlags")
    public CaseFlags getCaseFlags() {
        return this.caseFlags;
    }

    public void setCaseFlags(CaseFlags caseFlags) {
        this.caseFlags = caseFlags;
    }

    CaseFlags caseFlags;

    @JsonProperty("screenFlow")
    public ArrayList<ScreenFlow> getScreenFlow() {
        return this.screenFlow;
    }

    public void setScreenFlow(ArrayList<ScreenFlow> screenFlow) {
        this.screenFlow = screenFlow;
    }

    ArrayList<ScreenFlow> screenFlow;

    @JsonProperty("vocabulary")
    public Object getVocabulary() {
        return this.vocabulary;
    }

    public void setVocabulary(Object vocabulary) {
        this.vocabulary = vocabulary;
    }

    Object vocabulary;
}
