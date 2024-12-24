package digit.web.models;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlaughterUnit {
  private  Integer id;
  private  String openTime ;
  private  String closeTime;

    
}