package digit.web.models;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Boundary {
    private long id;
    private String district;
    private long pincode;
    private long subwardno;
    private long wardno;
    private String officename;
    private String wardname;
    private BigDecimal latitude;
    private String statename;
    private BigDecimal longitude;
    private String divisionname; 
}
