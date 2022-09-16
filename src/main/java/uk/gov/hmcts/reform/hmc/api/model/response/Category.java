package uk.gov.hmcts.reform.hmc.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Category {

    private String categoryKey;
    @JsonIgnore private String serviceId;

    private String key;

    private String valueEn;

    private String valueCy;

    private String hintTextEn;

    private String hintTextCy;

    private Long lovOrder;

    private String parentCategory;

    private String parentKey;

    private String activeFlag;

    @Setter private List<Category> childNodes;
}
