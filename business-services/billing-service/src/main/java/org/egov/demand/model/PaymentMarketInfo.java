package org.egov.demand.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMarketInfo {
    private String paymentMode;
    private String fundCenter;
    private String fund;
    private String businessArea;
    private String additionalDetails;
    private BigDecimal totalDue;
    private BigDecimal totalAmountPaid;
    private String functionalArea;
    private String receiptNumber;
    private String transactionNumber;

    /**
     * {@code egcl_payment.tenantid} — the tenant the collecting user held when this receipt was
     * taken. For a CFC counter receipt this is a ward tenant ({@code mh.mumbai.zone1.warda}),
     * because the collect screen sends the operator's ward-level role tenant and
     * collection-services stamps it onto the payment.
     *
     * <p>Shallower for everything else: a citizen or online payment, a bulk collection and every
     * SYSTEM_MIGRATION row all sit at {@code mh.mumbai}. Depth is therefore the test, not presence.
     *
     * <p>Distinct from {@link #businessArea} and the other three dimensions, which come from the
     * MARKET the licensee's stall is in and say nothing about where the money changed hands.
     */
    private String paymentTenantId;
}

