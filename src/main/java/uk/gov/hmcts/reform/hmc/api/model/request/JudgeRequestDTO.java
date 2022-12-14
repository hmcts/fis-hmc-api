package uk.gov.hmcts.reform.hmc.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(builderMethodName = "judgeRequestWith")
@Schema(description = "The request object to get judge details")
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class JudgeRequestDTO {
    @JsonProperty("personal_code")
    private List<String> personalCode;
}
