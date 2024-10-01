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

    @JsonProperty("animalType")
    private String animalType;

    @JsonProperty("count")
    private Integer count;
}