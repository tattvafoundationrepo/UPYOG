package digit.web.models;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class PeEnquiry {

        private int id;
        private String enquiryCode;
        private String oldenquiryCode; 
        private Date orderDate; 
        private String orderNo;
        private String enquirySubject; 
        private String employee; 
        private Department department; 
        private Designation designation; 
        private String tenantId;
        private Long createdAt;
        private Long updatedAt;
        private String createdBy;
        private String updatedBy;
        //private Long ordDate;
    
  
    
}
