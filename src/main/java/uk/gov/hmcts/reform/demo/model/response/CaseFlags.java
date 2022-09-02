package uk.gov.hmcts.reform.demo.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class CaseFlags {
    @JsonProperty("flags")
    public ArrayList<Object> getFlags() {
        return this.flags;
    }

    public void setFlags(ArrayList<Object> flags) {
        this.flags = flags;
    }

    ArrayList<Object> flags;

    @JsonProperty("flagAmendURL")
    public String getFlagAmendURL() {
        return this.flagAmendURL;
    }

    public void setFlagAmendURL(String flagAmendURL) {
        this.flagAmendURL = flagAmendURL;
    }

    String flagAmendURL;
}
