package digit.web.models.shopkeeper;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import digit.web.models.SlaughterList;
import lombok.Builder;


@Builder
public class ShopkeeperResponse {


     @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("animalAssignments")
    private List<ShopkeeperDetails> ShopkeeperDetails;

    @JsonProperty("slaughterLists")
    private List<SlaughterList> slaughterListDetails;

}
