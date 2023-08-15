package uk.gov.hmcts.reform.hmc.api.model.ccd.flagdata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@lombok.Data
@Builder(toBuilder = true)
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class FlagDetail {
    //  public List<Element<Path>> path;
    public String hearingRelevant;
    public Date dateTimeCreated;
    public String flagComment;
    public String subTypeKey;
    public String flagCode;
    public String name;
    public String subTypeValue;
    // public Object otherDescription;
    public String status;
}
