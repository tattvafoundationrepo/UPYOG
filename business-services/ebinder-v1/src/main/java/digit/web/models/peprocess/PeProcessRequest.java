package digit.web.models.peprocess;

import java.util.HashMap;

import org.egov.common.contract.request.RequestInfo;


import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import java.util.Map;


@Builder
@Data
public class PeProcessRequest {

    @JsonProperty("RequestInfo")
    RequestInfo requestInfo;

    private Map<String, Object> data = new HashMap<>();

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    @JsonAnySetter
    public void setData(String key, Object value) {
        data.put(key, value);
    }
}
