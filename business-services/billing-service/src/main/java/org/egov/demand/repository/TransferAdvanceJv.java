package org.egov.demand.repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.egov.demand.model.Demand;
import org.egov.demand.model.DemandDetail;
import org.egov.demand.model.FiReport;
import org.egov.demand.model.FiReportType;

/**
 * The marker emarket-v1 puts on a transfer's carried demand that holds the old licence's unused advance (BMC
 * 05-11-2026): {@code additionalDetails.transferAdvanceJv = {applicationNo, sources: [{consumerCode, amount}]}}, each
 * amount positive, together the advance carried. The advance moves between licences the way the outstanding does
 * ({@link TransferJv}), but on the advance liability and in the COLLECTION file, where the advance was booked:
 * Dr 350410215 under each source licence, Cr 350410215 under the new licence, same dimensions (the shop is the same).
 *
 * <p>A demand that is nothing but the carried advance then posts no demand voucher: its old one was Dr and Cr the
 * same account under the same licence, negative, and moved nothing. A demand that also carries dues keeps its
 * voucher as it is; its advance line there draws the advance down under the new licence, where this JV has just put it.
 */
public final class TransferAdvanceJv {

    public static final String KEY = "transferAdvanceJv";

    static final String ADVANCE_LIABILITY_GL = "350410215";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TransferAdvanceJv() {}

    /** One source licence's share of the carried advance. */
    public static final class Part {
        public final String consumerCode;
        public final BigDecimal amount;

        Part(String consumerCode, BigDecimal amount) {
            this.consumerCode = consumerCode;
            this.amount = amount;
        }
    }

    /** The JV to post for this demand: its parts and the transfer's application number. */
    public static final class Plan {
        public final String applicationNo;
        public final BigDecimal amount;
        public final List<Part> parts;

        Plan(String applicationNo, BigDecimal amount, List<Part> parts) {
            this.applicationNo = applicationNo;
            this.amount = amount;
            this.parts = parts;
        }
    }

    /** True when the demand carries the marker at all, well-formed or not. */
    public static boolean isMarked(Demand demand) {
        return raw(demand) != null;
    }

    static boolean isAdvanceHead(String taxHead) {
        return taxHead != null && taxHead.toUpperCase().contains("ADVANCE_CARRYFORWARD");
    }

    /** The advance this demand carries: minus the sum of its carry-forward lines (they are negative). */
    static BigDecimal carriedAdvance(Demand demand) {
        BigDecimal sum = BigDecimal.ZERO;
        if (demand == null || demand.getDemandDetails() == null)
            return sum;
        for (DemandDetail d : demand.getDemandDetails()) {
            if (d != null && isAdvanceHead(d.getTaxHeadMasterCode()) && d.getTaxAmount() != null)
                sum = sum.add(d.getTaxAmount());
        }
        return sum.negate();
    }

    /** True when every non-zero line of the demand is a carry-forward line: the demand is only the advance. */
    public static boolean isAdvanceOnly(Demand demand) {
        if (demand == null || demand.getDemandDetails() == null || demand.getDemandDetails().isEmpty())
            return false;
        boolean any = false;
        for (DemandDetail d : demand.getDemandDetails()) {
            if (d == null || d.getTaxAmount() == null || d.getTaxAmount().signum() == 0)
                continue;
            if (!isAdvanceHead(d.getTaxHeadMasterCode()))
                return false;
            any = true;
        }
        return any;
    }

    /**
     * The JV for this demand, or null when it carries no marker or a marker that does not describe exactly the
     * advance on the demand (no application number, no source, a non-positive part, or parts that do not add up to
     * the carry-forward lines to the paisa). Null means: post the ordinary voucher, as before. Total: never throws.
     */
    public static Plan plan(Demand demand) {
        Object marker = raw(demand);
        if (marker == null)
            return null;
        try {
            JsonNode node = MAPPER.valueToTree(marker);
            JsonNode app = node.get("applicationNo");
            JsonNode list = node.get("sources");
            if (app == null || app.isNull() || app.asText().trim().isEmpty() || list == null || !list.isArray())
                return null;
            BigDecimal carried = carriedAdvance(demand).setScale(2, RoundingMode.HALF_UP);
            if (carried.signum() <= 0)
                return null;
            List<Part> parts = new ArrayList<>();
            BigDecimal sum = BigDecimal.ZERO;
            for (JsonNode s : list) {
                JsonNode code = s.get("consumerCode");
                JsonNode amount = s.get("amount");
                if (code == null || code.isNull() || code.asText().trim().isEmpty() || amount == null
                        || !amount.isNumber())
                    return null;
                BigDecimal a = amount.decimalValue().setScale(2, RoundingMode.HALF_UP);
                if (a.signum() <= 0)
                    return null;
                parts.add(new Part(code.asText().trim(), a));
                sum = sum.add(a);
            }
            if (parts.isEmpty() || sum.compareTo(carried) != 0)
                return null;
            return new Plan(app.asText().trim(), carried, Collections.unmodifiableList(parts));
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * The JV's collection-table rows: Dr 350410215 per source (old licence reference), Cr 350410215 for the whole
     * amount (new licence reference). Every row carries the new demand's id, the demand's dimensions and dates, and the
     * transfer's application number as document header: a demand's sequence number shares its number space with
     * migrated receipt numbers, and collection-table readers find a receipt by that header.
     */
    static List<FiReport> rows(Demand demand, Plan plan, Long postingDate, String fund, String fundCenter,
                               String businessArea, String functionalArea) {
        List<FiReport> rows = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (Part p : plan.parts)
            rows.add(row(demand, "40", p.consumerCode, p.amount, plan.applicationNo, postingDate, fund, fundCenter,
                    businessArea, functionalArea, now));
        rows.add(row(demand, "50", demand.getConsumerCode(), plan.amount, plan.applicationNo, postingDate, fund,
                fundCenter, businessArea, functionalArea, now));
        return rows;
    }

    private static FiReport row(Demand demand, String postingKey, String referenceNo, BigDecimal amount,
                                String headerText, Long postingDate, String fund, String fundCenter,
                                String businessArea, String functionalArea, long now) {
        return FiReport.builder()
                .transactionNumber(demand.getId())
                .docDate(postingDate)
                .postingDate(postingDate)
                .referenceNo(referenceNo)
                .documentHeaderText(headerText)
                .postingKey(postingKey)
                .glCode(ADVANCE_LIABILITY_GL)
                .collectionAmount(amount)
                .fund(fund)
                .fundCentre(fundCenter)
                .businessArea(businessArea)
                .functionalArea(functionalArea)
                .remarks("Advance")
                .docType("YY")
                .reportType(FiReportType.UPMKT_COL_TRF_JV)
                .isNew(Boolean.TRUE)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private static Object raw(Demand demand) {
        if (demand == null || demand.getAdditionalDetails() == null)
            return null;
        Object ad = demand.getAdditionalDetails();
        Object v = null;
        if (ad instanceof Map)
            v = ((Map<?, ?>) ad).get(KEY);
        else if (ad instanceof JsonNode)
            v = ((JsonNode) ad).get(KEY);
        if (v instanceof JsonNode && ((JsonNode) v).isNull())
            return null;
        return v;
    }
}
