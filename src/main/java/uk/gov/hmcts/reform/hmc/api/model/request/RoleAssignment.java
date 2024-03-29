package uk.gov.hmcts.reform.hmc.api.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleAssignment implements Serializable {
    private static final long serialVersionUID = -6558703031023866825L;

    private String id;
    private String actorIdType;
    private String actorId;
    private String roleType;
    private String roleName;
    private String classification;
    private String grantType;
    private String roleCategory;
    private Boolean readOnly;

    @JsonFormat(timezone = "UTC", shape = JsonFormat.Shape.STRING)
    private Instant beginTime;

    @JsonFormat(timezone = "UTC", shape = JsonFormat.Shape.STRING)
    private Instant endTime;

    @JsonFormat(timezone = "UTC", shape = JsonFormat.Shape.STRING)
    private Instant created;

    private List<String> authorisations;
    private RoleAssignmentAttributesResource attributes;
}
