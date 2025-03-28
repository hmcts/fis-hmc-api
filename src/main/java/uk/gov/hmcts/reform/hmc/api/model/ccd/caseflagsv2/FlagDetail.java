package uk.gov.hmcts.reform.hmc.api.model.ccd.caseflagsv2;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.api.model.ccd.Element;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FlagDetail {
    public String name;
    @SuppressWarnings({"MemberName"})
    public String name_cy;
    public String subTypeValue;
    @SuppressWarnings({"MemberName"})
    public String subTypeValue_cy;
    public String subTypeKey;
    public String otherDescription;
    @SuppressWarnings({"MemberName"})
    public String otherDescription_cy;
    public String flagComment;
    @SuppressWarnings({"MemberName"})
    public String flagComment_cy;
    public String flagUpdateComment;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public LocalDateTime dateTimeCreated;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public LocalDateTime dateTimeModified;
    public List<Element<String>> path;
    public String hearingRelevant;
    public String flagCode;
    public String status;
    public String availableExternally;
}
