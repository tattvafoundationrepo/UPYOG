package digit.web.models.security;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnimalDetail{
    
    @JsonProperty("animalTypeId")
    private Long animalTypeId;

    @JsonProperty("animalType")
    private String animalType;

    @JsonProperty("token")
    private Integer count;

    private Boolean tradable;

    private Boolean stable;

    private Boolean editable ;
}