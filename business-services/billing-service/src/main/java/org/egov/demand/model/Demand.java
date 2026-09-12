package org.egov.demand.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.demand.web.contract.User;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * A Object which holds the basic info about the revenue assessment for which the demand is generated like module name, consumercode, owner, etc.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Demand {

    @SafeHtml
    @JsonProperty("id")
    private String id;

    @SafeHtml
    @NotNull
    @JsonProperty("tenantId")
    private String tenantId;

    @SafeHtml
    @NotNull
    @JsonProperty("consumerCode")
    private String consumerCode;

    @SafeHtml
    @NotNull
    @JsonProperty("consumerType")
    private String consumerType;

    @SafeHtml
    @NotNull
    @JsonProperty("businessService")
    private String businessService;

    @Valid
    @JsonProperty("payer")
    private User payer;

    @NotNull
    @JsonProperty("taxPeriodFrom")
    private Long taxPeriodFrom;

    @NotNull
    @JsonProperty("taxPeriodTo")
    private Long taxPeriodTo;

    private String licenceeName;
    private String licenceeMobile;

    @Default
    @JsonProperty("demandDetails")
    @Valid
    @NotNull
    @Size(min = 1)
    private List<DemandDetail> demandDetails = new ArrayList<>();

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;

    @JsonProperty("fixedBillExpiryDate")
    private Long fixedBillExpiryDate;

    @JsonProperty("billExpiryTime")
    private Long billExpiryTime;

    @JsonProperty("additionalDetails")
    private Object additionalDetails;

    private Long demandSeqNo;

    private String reason;

    private String fiReceiptNo;

    @Default
    @JsonProperty("minimumAmountPayable")
    private BigDecimal minimumAmountPayable = BigDecimal.ZERO;

    @Default
    private Boolean isPaymentCompleted = false;

    @Default
    @JsonProperty("isAdvance")
    private Boolean isAdvance = false;

    @Default
    @JsonProperty("advanceIndex")
    private Integer advanceIndex = 0;

    @JsonProperty("status")
    private StatusEnum status;

    private String paymentMode;

    public Demand addDemandDetailsItem(DemandDetail demandDetailsItem) {
        this.demandDetails.add(demandDetailsItem);
        return this;
    }
    

     private String fund;
       private String fundCenter;
       private String businessArea;
       private String functionalArea;

    /**
     * In-memory marker (within a single create() request): set when this newly created demand was
     * apportioned against a previously-collected advance. Used by DemandRepository.save() to tag the
     * demand FI rows as UPMKT_DEMDADV. Not persisted/serialized.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    private boolean apportionedAgainstAdvance = false;

    /**
     * In-memory marker (within a single collection or cancellation): the SAP dimensions of the CFC
     * where the money was actually taken, when that differs from the licensee's market.
     *
     * <p>The four fields above stay the licensee's market throughout — they are what the credit
     * legs and the whole demand side post to. This carries the collecting location alongside, for
     * the debit legs only, so one voucher can name both without either being overwritten.
     *
     * <p>Null means "no collecting ward established", which is the normal case for an online or
     * citizen payment, a bulk collection and every migrated receipt. Not persisted or serialised.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    private FiDimensions collectingDimensions;

    /**
     * In-memory marker (within a single cancellation): the dimensions each leg of the ORIGINAL
     * receipt was posted with, keyed by {@code glCode + "@" + forwardPostingKey}.
     *
     * <p>Used instead of {@link #collectingDimensions} on a reversal. A reversal must give back
     * exactly what was posted, and reading one row for the whole voucher cannot do that: the legs
     * of one receipt do not all share a dimension set once the collecting ward differs from the
     * market, and the rest of the reversal would otherwise take the licensee's CURRENT market,
     * which moves when a stall is re-pointed at another market.
     *
     * <p>Empty means "nothing posted to mirror", and every leg falls back to the market. Not
     * persisted or serialised.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.Map<String, FiDimensions> postedLegDimensions;

    /**
     * Gets or Sets status
     */
    public enum StatusEnum {

        ACTIVE("ACTIVE"),

        CANCELLED("CANCELLED"),

        ADJUSTED("ADJUSTED");

        private String value;

        StatusEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static StatusEnum fromValue(String text) {
            for (StatusEnum b : StatusEnum.values()) {
                if (String.valueOf(b.value).equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }

}
