package digit.web.models;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

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
public class EncryptionRequest {

    @NotNull
    @JsonProperty("encryptionRequests")
    private List<EncReqObject> encryptionRequests;


}
