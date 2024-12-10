package digit.web.models;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
@Component
public class EncReqObject {


        @NotNull
    @JsonProperty("tenantId")
    private String tenantId = null;

    @NotNull
    @JsonProperty("type")
    private String type = null;

    @NotNull
    @JsonProperty("value")
    @Valid
    private Object value = null;

}
