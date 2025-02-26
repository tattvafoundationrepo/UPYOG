package digit.web.models.security;

import lombok.Builder;
import lombok.Data;

import java.sql.Date;
import java.sql.Time;

import digit.web.models.inspection.Properties;

@Data
@Builder
public class Arrival {
    private Long id;
    private String arrivalId;
    private String importPermission;
    private Integer stakeholderId;
    private Date permissionDate;
    private Time timeOfArrival;
    private String vehicleNumber;
    private String licenceNumber;
    private Long createdAt;
    private Long updatedAt;
    private Integer createdBy;
    private Integer updatedBy;
    private Properties receiptMode;
    private String paymentReferenceNumber;
}