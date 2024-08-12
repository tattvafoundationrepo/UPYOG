package digit.web.models.scheme;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OccupationDTO {

     @JsonProperty("label")
     private String label;
      
     @JsonProperty("value")
     private String value;

}
