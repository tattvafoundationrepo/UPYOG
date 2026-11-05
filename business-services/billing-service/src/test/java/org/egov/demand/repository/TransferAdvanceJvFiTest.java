package org.egov.demand.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.egov.demand.model.Demand;
import org.egov.demand.model.DemandDetail;
import org.egov.demand.model.FiReport;
import org.egov.demand.model.FiReportType;
import org.egov.demand.web.contract.DemandRequest;
import org.junit.jupiter.api.Test;

/**
 * BMC 05-11-2026: a transfer's carried advance moves like the carried outstanding, by a JV, but on the advance
 * liability and in the collection file: Dr 350410215 old licence / Cr 350410215 new licence, positive, same
 * dimensions. Runs the real {@link DemandRepository#save} with only the database writes captured.
 */
public class TransferAdvanceJvFiTest {

    static final String ADV = "TX.EMARKET_RENTAL_ADVANCE_CARRYFORWARD";
    static final String NEW = "5888889106rf";
    static final String OLD = "5000002617rf";
    static final long NOV = 1793491200000L; // 01-Nov-2026 05:30 IST, the MDMS month start
    static final String APP = "APP-2026-11-04-001457";

    /** The repository with its writes captured instead of executed. */
    static final class Captured extends DemandRepository {
        final List<FiReport> demandRows = new ArrayList<>();
        final List<FiReport> collectionRows = new ArrayList<>();

        @Override
        public void insertBatch(List<Demand> newDemands, List<DemandDetail> newDemandDetails) {}

        @Override
        public void insertBatchForAudit(List<Demand> demands, List<DemandDetail> demandDetails) {}

        @Override
        public Map<String, Long> fetchDemandSeqNoByDemandIds(Set<String> demandIds) {
            Map<String, Long> m = new HashMap<>();
            for (String id : demandIds)
                m.put(id, 700100200L); // a number that is also a migrated receipt number on UAT
            return m;
        }

        @Override
        public void batchInsertDemandFiReports(List<FiReport> reports) {
            demandRows.addAll(reports);
        }

        @Override
        public void batchInsertCollectionFiReports(List<FiReport> reports) {
            collectionRows.addAll(reports);
        }
    }

    static DemandDetail line(String head, String amount, String gl) {
        Map<String, Object> add = new HashMap<>();
        if (gl != null)
            add.put("glcode", gl);
        return DemandDetail.builder().id(head + "-id").taxHeadMasterCode(head).taxAmount(new BigDecimal(amount))
                .collectionAmount(BigDecimal.ZERO).additionalDetails(add).build();
    }

    static Map<String, Object> part(String code, Object amount) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("consumerCode", code);
        p.put("amount", amount);
        return p;
    }

    static Map<String, Object> marker(String app, List<Map<String, Object>> parts) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("applicationNo", app);
        m.put("sources", parts);
        return m;
    }

    static Demand demand(String id, Map<String, Object> marker, DemandDetail... lines) {
        Map<String, Object> add = new HashMap<>();
        add.put("fund", "11");
        add.put("fundCenter", "4030420101");
        add.put("businessArea", "4030");
        add.put("functionalArea", "55800000000");
        if (marker != null)
            add.put(TransferAdvanceJv.KEY, marker);
        return Demand.builder().id(id).tenantId("mh.mumbai").consumerCode(NEW)
                .businessService("TX.Emarket_Rental_Fees").taxPeriodFrom(NOV).taxPeriodTo(NOV + 1L)
                .advanceIndex(-1).demandDetails(new ArrayList<>(Arrays.asList(lines))).additionalDetails(add).build();
    }

    static Captured save(Demand... demands) {
        Captured repo = new Captured();
        DemandRequest req = new DemandRequest();
        req.setDemands(new ArrayList<>(Arrays.asList(demands)));
        repo.save(req);
        return repo;
    }

    static String render(List<FiReport> rows) {
        List<String> out = new ArrayList<>();
        for (FiReport r : rows)
            out.add(r.getReportType() + " " + r.getPostingKey() + " " + r.getGlCode() + " " + r.getReferenceNo() + " "
                    + r.getCollectionAmount() + " " + r.getRemarks());
        Collections.sort(out);
        return String.join("\n", out);
    }

    // --------------------------------------------------------------------------------------------- advance only

    @Test
    public void advanceOnly_postsTheJvInTheCollectionFile_andNoDemandVoucher() {
        Captured r = save(demand("d-1", marker(APP, Arrays.asList(part(OLD, 1020))), line(ADV, "-1020.00", "350410215")));
        assertTrue(r.demandRows.isEmpty(), "the self-cancelling demand voucher is gone: " + render(r.demandRows));
        assertEquals(2, r.collectionRows.size());
        FiReport dr = r.collectionRows.get(0), cr = r.collectionRows.get(1);
        assertEquals("40", dr.getPostingKey());
        assertEquals(OLD, dr.getReferenceNo());
        assertEquals("50", cr.getPostingKey());
        assertEquals(NEW, cr.getReferenceNo());
        for (FiReport row : r.collectionRows) {
            assertEquals("350410215", row.getGlCode());
            assertEquals(0, new BigDecimal("1020").compareTo(row.getCollectionAmount()), "positive amounts");
            assertEquals(FiReportType.UPMKT_COL_TRF_JV, row.getReportType());
            assertEquals("d-1", row.getTransactionNumber());
            assertEquals(APP, row.getDocumentHeaderText(), "never the demand seq no, which collides with receipts");
            assertEquals("YY", row.getDocType());
            assertEquals("Advance", row.getRemarks());
            assertEquals(Long.valueOf(NOV), row.getDocDate());
            assertEquals(Long.valueOf(NOV), row.getPostingDate());
            // Same shop, same market: both licences carry the same dimensions.
            assertEquals("11", row.getFund());
            assertEquals("4030420101", row.getFundCentre());
            assertEquals("4030", row.getBusinessArea());
            assertEquals("55800000000", row.getFunctionalArea());
            assertNull(row.getAssignment());
        }
    }

    @Test
    public void amalgamation_oneDebitPerOldLicence_oneCreditForTheWhole() {
        Captured r = save(demand("d-2", marker(APP, Arrays.asList(part("5000008518rf", 100), part("5000008519rf", 200.5))),
                line(ADV, "-300.50", "350410215")));
        assertTrue(r.demandRows.isEmpty());
        assertEquals(String.join("\n",
                "upmktcoltrfjv 40 350410215 5000008518rf 100.00 Advance",
                "upmktcoltrfjv 40 350410215 5000008519rf 200.50 Advance",
                "upmktcoltrfjv 50 350410215 " + NEW + " 300.50 Advance"), render(r.collectionRows));
        BigDecimal dr = BigDecimal.ZERO, cr = BigDecimal.ZERO;
        for (FiReport row : r.collectionRows) {
            if ("40".equals(row.getPostingKey())) dr = dr.add(row.getCollectionAmount());
            else cr = cr.add(row.getCollectionAmount());
        }
        assertEquals(0, dr.compareTo(cr), "the document balances");
    }

    @Test
    public void markerAsJson_isReadToo() throws Exception {
        Demand d = demand("d-3", null, line(ADV, "-1675", "350410215"));
        Map<String, Object> add = new HashMap<>();
        add.put(TransferAdvanceJv.KEY, new ObjectMapper().valueToTree(marker(APP, Arrays.asList(part(OLD, 1675)))));
        add.put("fundCenter", "4130420104");
        d.setAdditionalDetails(add);
        Captured r = save(d);
        assertEquals(2, r.collectionRows.size());
        assertTrue(r.demandRows.isEmpty());
    }

    // ------------------------------------------------------------------------------------------- mixed demand

    @Test
    public void mixedDemand_keepsItsVoucherByteForByte_andAddsTheJvForItsAdvanceOnly() {
        DemandDetail[] lines = { line("STALLAGE", "5785.00", "130100301"), line("CGST", "520.65", "350200421"),
                line("SGST", "520.65", "350200422"), line(ADV, "-0.20", "350410215") };
        Captured before = save(demand("d-4", null, lines));
        Captured after = save(demand("d-4", marker(APP, Arrays.asList(part("5000016485rf", 0.20))),
                line("STALLAGE", "5785.00", "130100301"), line("CGST", "520.65", "350200421"),
                line("SGST", "520.65", "350200422"), line(ADV, "-0.20", "350410215")));
        assertFalse(before.demandRows.isEmpty());
        assertEquals(render(before.demandRows), render(after.demandRows));
        assertTrue(before.collectionRows.isEmpty());
        assertEquals(String.join("\n",
                "upmktcoltrfjv 40 350410215 5000016485rf 0.20 Advance",
                "upmktcoltrfjv 50 350410215 " + NEW + " 0.20 Advance"), render(after.collectionRows));
    }

    // ------------------------------------------------------------------------------------ unmarked / malformed

    @Test
    public void unmarked_advanceDemand_postsExactlyWhatItAlwaysDid() {
        // The shape on UAT for 5888889106rf: Dr and Cr 350410215, both -1,020, same licence.
        Captured r = save(demand("d-5", null, line(ADV, "-1020.00", "350410215")));
        assertTrue(r.collectionRows.isEmpty());
        assertEquals(2, r.demandRows.size(), render(r.demandRows));
        for (FiReport row : r.demandRows) {
            assertEquals(FiReportType.UPMKT_DEMD, row.getReportType());
            assertEquals(NEW, row.getReferenceNo());
            assertEquals(0, new BigDecimal("-1020").compareTo(row.getCollectionAmount()));
        }
    }

    @Test
    public void malformedMarkers_fallBackToTheOrdinaryVoucher() {
        Captured plain = save(demand("d-6", null, line(ADV, "-1020.00", "350410215")));
        List<Map<String, Object>> bad = new ArrayList<>();
        bad.add(marker(APP, Arrays.asList(part(OLD, 1000))));                         // parts short of the advance
        bad.add(marker(APP, Arrays.asList(part(OLD, 1020), part("5000000001rf", 1)))); // parts over it
        bad.add(marker(null, Arrays.asList(part(OLD, 1020))));                        // no application number
        bad.add(marker(" ", Arrays.asList(part(OLD, 1020))));
        bad.add(marker(APP, Arrays.asList(part(OLD, -1020))));                        // negative part
        bad.add(marker(APP, Arrays.asList(part(OLD, 0), part("5000000001rf", 1020)))); // zero part
        bad.add(marker(APP, Arrays.asList(part(null, 1020))));                        // no consumer code
        bad.add(marker(APP, Arrays.asList(part(OLD, "1020"))));                       // amount not a number
        bad.add(marker(APP, Collections.<Map<String, Object>>emptyList()));
        for (Map<String, Object> m : bad) {
            Captured r = save(demand("d-6", m, line(ADV, "-1020.00", "350410215")));
            assertTrue(r.collectionRows.isEmpty(), "no JV for " + m);
            assertEquals(render(plain.demandRows), render(r.demandRows), "ordinary voucher for " + m);
        }
        Map<String, Object> noSources = new LinkedHashMap<>();
        noSources.put("applicationNo", APP);
        Captured r = save(demand("d-6", noSources, line(ADV, "-1020.00", "350410215")));
        assertTrue(r.collectionRows.isEmpty());
        assertEquals(render(plain.demandRows), render(r.demandRows));
    }

    @Test
    public void markerOnADemandWithNoAdvanceLine_changesNothing() {
        Captured plain = save(demand("d-7", null, line("STALLAGE", "325", "130100301")));
        Captured r = save(demand("d-7", marker(APP, Arrays.asList(part(OLD, 325))), line("STALLAGE", "325", "130100301")));
        assertTrue(r.collectionRows.isEmpty());
        assertEquals(render(plain.demandRows), render(r.demandRows));
    }

    @Test
    public void batch_eachCarriedAdvanceGetsItsOwnJv_otherDemandsUntouched() {
        Captured plain = save(demand("x", null, line("STALLAGE", "325", "130100301")));
        Captured r = save(
                demand("a", marker(APP, Arrays.asList(part(OLD, 600))), line(ADV, "-600", "350410215")),
                demand("x", null, line("STALLAGE", "325", "130100301")),
                demand("b", marker(APP, Arrays.asList(part(OLD, 400))), line(ADV, "-400", "350410215")));
        assertEquals(render(plain.demandRows), render(r.demandRows));
        assertEquals(4, r.collectionRows.size());
        Map<String, BigDecimal> credited = new TreeMap<>();
        for (FiReport row : r.collectionRows)
            if ("50".equals(row.getPostingKey()))
                credited.put(row.getTransactionNumber(), row.getCollectionAmount());
        assertEquals("{a=600.00, b=400.00}", credited.toString());
    }

    // ---------------------------------------------------------------------------------------------- the parser

    @Test
    public void plan_andAdvanceOnly_edges() {
        assertNull(TransferAdvanceJv.plan(null));
        assertNull(TransferAdvanceJv.plan(demand("e", null, line(ADV, "-1", null))));
        Demand d = demand("e", marker(APP, Arrays.asList(part(OLD, 1020.004))), line(ADV, "-1020.00", null));
        assertNotNull(TransferAdvanceJv.plan(d), "rounded to the paisa");
        assertTrue(TransferAdvanceJv.isAdvanceOnly(demand("e", null, line(ADV, "-1", null), line("STALLAGE", "0", null))));
        assertFalse(TransferAdvanceJv.isAdvanceOnly(demand("e", null, line(ADV, "-1", null), line("STALLAGE", "1", null))));
        assertFalse(TransferAdvanceJv.isAdvanceOnly(demand("e", null)));
        // A carry-forward line that is somehow positive is no advance to move.
        assertNull(TransferAdvanceJv.plan(demand("e", marker(APP, Arrays.asList(part(OLD, 5))), line(ADV, "5", null))));
    }
}
