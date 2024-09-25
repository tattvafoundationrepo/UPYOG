package digit.web.models;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Designation {

    private String code;
    private String name;
    private String description;
    private boolean active;
    private String i18key;

    public Designation(String i18key){
        this.i18key = i18key;
    }

}
