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

}
