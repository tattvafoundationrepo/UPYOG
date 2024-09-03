package digit.web.models.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdatedDocument {
    
    @JsonProperty("document")
    DocumentDetails documentDetails;
    
    @JsonProperty("documentNo")
    private String documentNumber;

}
