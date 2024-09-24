package digit.web.models;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PeEnquiryResponseWrapper {


    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("peEnquiryList")
    private List<PeEnquiryResponse> enquiryList;

}
