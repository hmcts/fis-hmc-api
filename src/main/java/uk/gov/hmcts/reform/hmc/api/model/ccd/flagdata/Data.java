package uk.gov.hmcts.reform.hmc.api.model.ccd.flagdata;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@lombok.Data
@Builder(toBuilder = true)
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class Data {
    public List<Path> path;
    public String hearingRelevant;
    public Date dateTimeCreated;
    public String flagComment;
    public Object subTypeKey;
    public String flagCode;
    public String name;
    public Object subTypeValue;
    public Object otherDescription;
    public String status;
}
