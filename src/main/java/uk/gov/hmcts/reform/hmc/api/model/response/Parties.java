package uk.gov.hmcts.reform.hmc.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "partyDetailsWith")
@NoArgsConstructor
@AllArgsConstructor
public class Parties {

    private String categoryType;

    private String categoryValue;

    private String categoryParent;
}
