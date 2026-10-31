package org.egov.demand.repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.egov.demand.model.Demand;

/**
 * The marker emarket-v1 puts on a demand that carries an old licence's unpaid dues to a new licence on transfer
 * (BMC 30-10-2026): {@code additionalDetails.transferJv = {applicationNo, sources: [{consumerCode, heads: {head:
 * amount}}]}}. Such a demand posts a reclassification JV instead of a demand voucher: each 4-Series receivable it
 * would have debited is debited (new licence reference) and credited to the source licence(s) (old licence
 * reference). Revenue and GST were recognised once, on the old licence's own demands, and are not posted again.
 */
public final class TransferJv {

    public static final String KEY = "transferJv";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TransferJv() {}

    /** One source licence's share of a carried demand. */
    public static final class Source {
        public final String consumerCode;
        public final Map<String, BigDecimal> heads;

        Source(String consumerCode, Map<String, BigDecimal> heads) {
            this.consumerCode = consumerCode;
            this.heads = heads;
        }

        BigDecimal total() {
            BigDecimal t = BigDecimal.ZERO;
            for (BigDecimal v : heads.values())
                t = t.add(v);
            return t;
        }
    }

    /** True when the demand carries the marker at all, well-formed or not. */
    public static boolean isMarked(Demand demand) {
        return raw(demand) != null;
    }

    /**
     * The sources named by the marker, or null when the demand carries no marker or no usable source. Total: never
     * throws.
     */
    public static List<Source> sources(Demand demand) {
        Object marker = raw(demand);
        if (marker == null)
            return null;
        try {
            JsonNode node = MAPPER.valueToTree(marker);
            JsonNode list = node.get("sources");
            if (list == null || !list.isArray())
                return null;
            List<Source> out = new ArrayList<>();
            for (JsonNode s : list) {
                JsonNode code = s.get("consumerCode");
                if (code == null || code.isNull() || code.asText().trim().isEmpty())
                    continue;
                Map<String, BigDecimal> heads = new LinkedHashMap<>();
                JsonNode h = s.get("heads");
                if (h != null && h.isObject()) {
                    h.fields().forEachRemaining(e -> {
                        if (e.getValue() != null && e.getValue().isNumber())
                            heads.put(e.getKey(), e.getValue().decimalValue());
                    });
                }
                out.add(new Source(code.asText().trim(), heads));
            }
            return out.isEmpty() ? null : out;
        } catch (RuntimeException e) {
            return null;
        }
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

    /**
     * Splits one receivable line across the sources: by each source's share of that tax head, or of the whole
     * demand when no source names the head (a round-off line, say). Rounded to the paisa; the last weighted source
     * takes the remainder, so the parts always add up to {@code amount}. A single source takes it all.
     */
    public static Map<String, BigDecimal> split(BigDecimal amount, String taxHeadCode, List<Source> sources) {
        Map<String, BigDecimal> out = new LinkedHashMap<>();
        if (sources == null || sources.isEmpty() || amount == null)
            return out;
        if (sources.size() == 1) {
            out.put(sources.get(0).consumerCode, amount);
            return out;
        }
        List<BigDecimal> weights = new ArrayList<>();
        BigDecimal sum = BigDecimal.ZERO;
        for (Source s : sources) {
            BigDecimal w = s.heads.get(taxHeadCode);
            w = w == null || w.signum() < 0 ? BigDecimal.ZERO : w;
            weights.add(w);
            sum = sum.add(w);
        }
        if (sum.signum() == 0) {
            weights.clear();
            for (Source s : sources) {
                BigDecimal w = s.total().max(BigDecimal.ZERO);
                weights.add(w);
                sum = sum.add(w);
            }
        }
        if (sum.signum() == 0) {
            weights = new ArrayList<>(Collections.nCopies(sources.size(), BigDecimal.ZERO));
            weights.set(0, BigDecimal.ONE);
            sum = BigDecimal.ONE;
        }
        int last = -1;
        for (int i = 0; i < weights.size(); i++)
            if (weights.get(i).signum() > 0)
                last = i;
        BigDecimal placed = BigDecimal.ZERO;
        for (int i = 0; i < sources.size(); i++) {
            if (weights.get(i).signum() == 0)
                continue;
            BigDecimal part = i == last ? amount.subtract(placed)
                    : amount.multiply(weights.get(i)).divide(sum, 2, RoundingMode.HALF_UP);
            placed = placed.add(part);
            out.merge(sources.get(i).consumerCode, part, BigDecimal::add);
        }
        return out;
    }
}
