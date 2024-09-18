package digit.web.models.hrms;



import org.egov.common.contract.models.AuditDetails;
import org.springframework.validation.annotation.Validated;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Validated
@EqualsAndHashCode(exclude = {"auditDetails"})
@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Setter
@ToString
public class ServiceHistory {

	private String id;

	private String serviceStatus;

	private Long serviceFrom;

	private Long serviceTo;

	private String orderNo;
	
	private String location;
	
	private String tenantId;	

	private  Boolean isCurrentPosition;

	private AuditDetails auditDetails;



}
