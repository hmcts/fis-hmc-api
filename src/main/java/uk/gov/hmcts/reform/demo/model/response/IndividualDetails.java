package uk.gov.hmcts.reform.demo.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class IndividualDetails {
    @JsonProperty("firstName")
    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    String firstName;

    @JsonProperty("lastName")
    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    String lastName;

    @JsonProperty("preferredHearingChannel")
    public String getPreferredHearingChannel() {
        return this.preferredHearingChannel;
    }

    public void setPreferredHearingChannel(String preferredHearingChannel) {
        this.preferredHearingChannel = preferredHearingChannel;
    }

    String preferredHearingChannel;

    @JsonProperty("reasonableAdjustments")
    public ArrayList<Object> getReasonableAdjustments() {
        return this.reasonableAdjustments;
    }

    public void setReasonableAdjustments(ArrayList<Object> reasonableAdjustments) {
        this.reasonableAdjustments = reasonableAdjustments;
    }

    ArrayList<Object> reasonableAdjustments;

    @JsonProperty("vulnerableFlag")
    public boolean getVulnerableFlag() {
        return this.vulnerableFlag;
    }

    public void setVulnerableFlag(boolean vulnerableFlag) {
        this.vulnerableFlag = vulnerableFlag;
    }

    boolean vulnerableFlag;

    @JsonProperty("hearingChannelEmail")
    public ArrayList<Object> getHearingChannelEmail() {
        return this.hearingChannelEmail;
    }

    public void setHearingChannelEmail(ArrayList<Object> hearingChannelEmail) {
        this.hearingChannelEmail = hearingChannelEmail;
    }

    ArrayList<Object> hearingChannelEmail;

    @JsonProperty("hearingChannelPhone")
    public ArrayList<Object> getHearingChannelPhone() {
        return this.hearingChannelPhone;
    }

    public void setHearingChannelPhone(ArrayList<Object> hearingChannelPhone) {
        this.hearingChannelPhone = hearingChannelPhone;
    }

    ArrayList<Object> hearingChannelPhone;

    @JsonProperty("relatedParties")
    public ArrayList<Object> getRelatedParties() {
        return this.relatedParties;
    }

    public void setRelatedParties(ArrayList<Object> relatedParties) {
        this.relatedParties = relatedParties;
    }

    ArrayList<Object> relatedParties;
}
