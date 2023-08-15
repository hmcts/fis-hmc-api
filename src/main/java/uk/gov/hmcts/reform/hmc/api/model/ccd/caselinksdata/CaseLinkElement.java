package uk.gov.hmcts.reform.hmc.api.model.ccd.caselinksdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseLinkElement<T> {
    @JsonProperty("id")
    private String id;

    @NotNull
    @Valid
    @JsonProperty("value")
    private T value;
}
