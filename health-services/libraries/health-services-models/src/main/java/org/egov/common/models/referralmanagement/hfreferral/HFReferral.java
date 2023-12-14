package org.egov.common.models.referralmanagement.hfreferral;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import digit.models.coremodels.AuditDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.models.project.AdditionalFields;
import org.egov.common.models.referralmanagement.sideeffect.SideEffect;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HFReferral {

    @JsonProperty("id")
    @Size(min = 2, max = 64)
    private String id;

    @JsonProperty("clientReferenceId")
    @Size(min = 2, max = 64)
    private String clientReferenceId;

    @JsonProperty("tenantId")
    @NotNull
    @Size(min=2, max = 1000)
    private String tenantId;

    @JsonProperty("projectId")
    @Size(min = 2, max = 64)
    private String projectId;

    @JsonProperty("facilityId")
    @Size(min = 2, max = 64)
    private String facilityId;

    @JsonProperty("symptom")
    @NotNull
    @Size(min = 2, max = 500)
    private String symptom;

    @JsonProperty("symptomSurveyId")
    @NotNull
    @Size(min = 2, max = 100)
    private String symptomSurveyId;

    @JsonProperty("beneficiaryId")
    @Size(max=1000)
    private String beneficiaryId;

    @JsonProperty("referralCode")
    @Size(max=1000)
    private String referralCode;

    @JsonProperty("nationalLevelId")
    @Size(max=1000)
    private String nationalLevelId;

    @JsonProperty("isDeleted")
    private Boolean isDeleted = Boolean.FALSE;

    @JsonProperty("rowVersion")
    private Integer rowVersion;

    @JsonProperty("auditDetails")
    @Valid
    private AuditDetails auditDetails;

    @JsonProperty("clientAuditDetails")
    @Valid
    private AuditDetails clientAuditDetails;

    @JsonProperty("additionalFields")
    @Valid
    private AdditionalFields additionalFields;

    @JsonIgnore
    private Boolean hasErrors = Boolean.FALSE;
}
