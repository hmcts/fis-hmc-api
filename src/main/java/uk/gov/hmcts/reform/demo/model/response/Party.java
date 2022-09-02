package uk.gov.hmcts.reform.demo.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class Party {
    @JsonProperty("partyID")
    public String getPartyID() {
        return this.partyID;
    }

    public void setPartyID(String partyID) {
        this.partyID = partyID;
    }

    String partyID;

    @JsonProperty("partyType")
    public String getPartyType() {
        return this.partyType;
    }

    public void setPartyType(String partyType) {
        this.partyType = partyType;
    }

    String partyType;

    @JsonProperty("partyName")
    public String getPartyName() {
        return this.partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    String partyName;

    @JsonProperty("partyChannel")
    public String getPartyChannel() {
        return this.partyChannel;
    }

    public void setPartyChannel(String partyChannel) {
        this.partyChannel = partyChannel;
    }

    String partyChannel;

    @JsonProperty("partyRole")
    public String getPartyRole() {
        return this.partyRole;
    }

    public void setPartyRole(String partyRole) {
        this.partyRole = partyRole;
    }

    String partyRole;

    @JsonProperty("individualDetails")
    public IndividualDetails getIndividualDetails() {
        return this.individualDetails;
    }

    public void setIndividualDetails(IndividualDetails individualDetails) {
        this.individualDetails = individualDetails;
    }

    IndividualDetails individualDetails;

    @JsonProperty("organisationDetails")
    public Object getOrganisationDetails() {
        return this.organisationDetails;
    }

    public void setOrganisationDetails(Object organisationDetails) {
        this.organisationDetails = organisationDetails;
    }

    Object organisationDetails;

    @JsonProperty("unavailabilityRanges")
    public ArrayList<Object> getUnavailabilityRanges() {
        return this.unavailabilityRanges;
    }

    public void setUnavailabilityRanges(ArrayList<Object> unavailabilityRanges) {
        this.unavailabilityRanges = unavailabilityRanges;
    }

    ArrayList<Object> unavailabilityRanges;

    @JsonProperty("unavailabilityDOW")
    public ArrayList<Object> getUnavailabilityDOW() {
        return this.unavailabilityDOW;
    }

    public void setUnavailabilityDOW(ArrayList<Object> unavailabilityDOW) {
        this.unavailabilityDOW = unavailabilityDOW;
    }

    ArrayList<Object> unavailabilityDOW;
}
