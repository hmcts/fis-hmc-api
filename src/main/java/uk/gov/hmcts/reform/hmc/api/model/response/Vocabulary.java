package uk.gov.hmcts.reform.hmc.api.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "vocabularyWith")
@NoArgsConstructor
@AllArgsConstructor
public class Vocabulary {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String word1;
}
