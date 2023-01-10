package uk.gov.hmcts.reform.hmc.api.model.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder(builderMethodName = "judgeDetailWith")
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class JudgeDetail {

    @JsonProperty("full_name")
    @JsonAlias("full_name")
    private String hearingJudgeName;
}
