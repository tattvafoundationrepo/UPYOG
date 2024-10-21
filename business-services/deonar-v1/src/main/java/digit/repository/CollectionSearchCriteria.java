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
}
