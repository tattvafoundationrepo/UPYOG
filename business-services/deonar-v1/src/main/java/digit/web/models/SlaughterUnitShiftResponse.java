package digit.web.models;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SlaughterUnitShiftResponse {

    @JsonProperty("ResponseInfo")
    ResponseInfo responseInfo;

    List<SlaughterUnit> unit;


}
