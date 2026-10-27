package org.egov.demand.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GstAdvanceMap {


    private BigDecimal collectionAmount ;
	private	String collectionGlCode ;
	private	BigDecimal cgstAmount ;
	private	String cgstGlCode ;
	private	BigDecimal sgstAmount ;
	private	String sgstGlCode ;

	private BigDecimal totalAmountPaid ;
    private BigDecimal rentalAdvancePaid ;
    private BigDecimal licenseAdvancePaid ;

    /**
     * Paise the cashier rounded a cash receipt up by (0 &lt; x &lt; 1), or null. Included in
     * totalAmountPaid (money received) but never applied to a demand; posted to the Coin Note account.
     */
    private BigDecimal coinNoteAdjustment ;

}
