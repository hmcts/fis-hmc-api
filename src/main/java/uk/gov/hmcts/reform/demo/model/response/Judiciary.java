package uk.gov.hmcts.reform.demo.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class Judiciary {
    @JsonProperty("roleType")
    public ArrayList<Object> getRoleType() {
        return this.roleType;
    }

    public void setRoleType(ArrayList<Object> roleType) {
        this.roleType = roleType;
    }

    ArrayList<Object> roleType;

    @JsonProperty("authorisationTypes")
    public ArrayList<Object> getAuthorisationTypes() {
        return this.authorisationTypes;
    }

    public void setAuthorisationTypes(ArrayList<Object> authorisationTypes) {
        this.authorisationTypes = authorisationTypes;
    }

    ArrayList<Object> authorisationTypes;

    @JsonProperty("authorisationSubType")
    public ArrayList<Object> getAuthorisationSubType() {
        return this.authorisationSubType;
    }

    public void setAuthorisationSubType(ArrayList<Object> authorisationSubType) {
        this.authorisationSubType = authorisationSubType;
    }

    ArrayList<Object> authorisationSubType;

    @JsonProperty("panelComposition")
    public Object getPanelComposition() {
        return this.panelComposition;
    }

    public void setPanelComposition(Object panelComposition) {
        this.panelComposition = panelComposition;
    }

    Object panelComposition;

    @JsonProperty("judiciaryPreferences")
    public ArrayList<Object> getJudiciaryPreferences() {
        return this.judiciaryPreferences;
    }

    public void setJudiciaryPreferences(ArrayList<Object> judiciaryPreferences) {
        this.judiciaryPreferences = judiciaryPreferences;
    }

    ArrayList<Object> judiciaryPreferences;

    @JsonProperty("judiciarySpecialisms")
    public ArrayList<Object> getJudiciarySpecialisms() {
        return this.judiciarySpecialisms;
    }

    public void setJudiciarySpecialisms(ArrayList<Object> judiciarySpecialisms) {
        this.judiciarySpecialisms = judiciarySpecialisms;
    }

    ArrayList<Object> judiciarySpecialisms;
}
