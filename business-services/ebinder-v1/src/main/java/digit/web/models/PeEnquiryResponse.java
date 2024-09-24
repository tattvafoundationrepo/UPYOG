package digit.web.models;

import java.net.http.HttpResponse.ResponseInfo;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component

public class PeEnquiryResponse {

    @JsonProperty("data1")
    @Valid
    private PeEnquiry peEnquiry;
    
    @JsonProperty("data2")
    private List<EmployeeData> empData;


}
