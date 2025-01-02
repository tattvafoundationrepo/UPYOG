package digit.repository;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class CollectionSearchCriteria {
    @JsonProperty("Search")
    private String search;

    private String liceneceNumber;

    private Long stakeholderId;

    private String slaughterUnit;

    private String openTime;
    private String closeTime;

    
}
