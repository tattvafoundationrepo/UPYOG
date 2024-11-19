package digit.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GatePassDetails {

    private Long vehicleType;
    private String vehicleNumber;
    private String receiverName;
    private String receiverContact;
    private String typeOfAnimal;
    private Double carcasweight;
    private Double kenaweight;
    private String referenceNumber;
    private Long shopkeeper;
    private Long helkari;

    private Long createdDate;

    private Long lastModifiedDate;

    private Long createdBy;
    private Long lastModifiedBy;

}
