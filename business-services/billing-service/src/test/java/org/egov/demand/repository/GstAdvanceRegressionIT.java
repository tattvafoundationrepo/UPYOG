package org.egov.demand.repository;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;

import org.egov.demand.model.*;
import org.egov.demand.web.contract.DemandRequest;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Database-backed regression suite for the GST advance net-off.
 *
 * <p>The unit tests alongside this cover the arithmetic in isolation; these cover the parts that
 * only a real database can show — the balance cap, licence-level keying across business-service
 * suffixes, residual-based reversal, and the full cancellation unwind. Each was written for a
 * defect that reached review, so a failure here means that defect has returned.
 *
 * <p>Skipped automatically when no database is reachable, so it never breaks an offline build.
 * Point it elsewhere with -Dgst.it.url / -Dgst.it.user / -Dgst.it.password.
 */
class GstAdvanceRegressionIT {

    private static final String CGST_ADV = "439300200", SGST_ADV = "439300201";
    private static final String CGST_PAY = "350200421", ADVANCE = "350410215", RECEIVABLE = "431409936";

    private static JdbcTemplate jt;
    private static DemandRepository repo;
    private static boolean dbUp;

    @BeforeAll
    static void connect() throws Exception {
        String url = System.getProperty("gst.it.url", "jdbc:postgresql://localhost:5432/gst_verify");
        try {
            SingleConnectionDataSource ds = new SingleConnectionDataSource(url,
                    System.getProperty("gst.it.user", "postgres"),
                    System.getProperty("gst.it.password", "postgres"), true);
            ds.setDriverClassName("org.postgresql.Driver");
            jt = new JdbcTemplate(ds);
            jt.queryForObject("SELECT 1", Integer.class);
            dbUp = true;
        } catch (Exception e) {
            dbUp = false;
            return;
        }
        repo = new DemandRepository();
        set(repo, "jdbcTemplate", jt);
        set(repo, "receivableGlCode", RECEIVABLE);
        set(repo, "demandQueryBuilder", Class.forName(
                "org.egov.demand.repository.querybuilder.DemandQueryBuilder").getDeclaredConstructor().newInstance());
        Object util = Class.forName("org.egov.demand.util.Util").getDeclaredConstructor().newInstance();
        Field om = util.getClass().getDeclaredField("mapper"); om.setAccessible(true);
        om.set(util, new com.fasterxml.jackson.databind.ObjectMapper());
        set(repo, "util", util);
    }

    @BeforeEach
    void requireDb() {
        Assumptions.assumeTrue(dbUp, "no database reachable — integration checks skipped");
        for (String t : new String[]{ "eg_emarket_fi_report", "eg_emarket_fi_report_collection",
                "eg_emarket_demand_settlement_info", "egbs_demanddetail_v1", "egbs_demand_v1",
                "egbs_demanddetail_v1_audit", "egbs_demand_v1_audit" })
            jt.update("DELETE FROM " + t);
    }

    // ---------------------------------------------------------------- the cap

    @Test @DisplayName("net-off is bounded by the advance that actually exists")
    void capBoundsTheRelease() throws Exception {
        advance("5000000284rf", "100.00");
        assertEquals(0, netOff(demand("5000000284rf", "63.90", "63.90")).compareTo(new BigDecimal("63.90")),
                "a demand smaller than the advance releases its own GST");

        clearFi();
        advance("5000000284rf", "20.00");
        assertEquals(0, netOff(demand("5000000284rf", "63.90", "63.90")).compareTo(new BigDecimal("20.00")),
                "a demand larger than the advance is capped at what is left");

        clearFi();
        assertEquals(0, netOff(demand("5000000284rf", "63.90", "63.90")).compareTo(BigDecimal.ZERO),
                "a migrated licensee with no in-system advance nets nothing");
    }

    @Test @DisplayName("demands in one batch cannot jointly over-release the advance")
    void batchTallyBoundsTheRelease() {
        // Driven through the real save() rather than the reflection helper: the running tally is
        // committed by save(), so a helper-level test would keep passing if that wiring were ever
        // dropped. No FI row is written until after the loop, so all three demands read the same
        // committed 100.00 balance and only the tally can stop them releasing 63.90 each.
        advance("5000000284rf", "100.00");

        List<Demand> batch = new ArrayList<>();
        for (int i = 0; i < 3; i++)
            batch.add(saveable("5000000284rf", "D-BATCH-" + i, "63.90", i));

        DemandRequest req = new DemandRequest();
        req.setDemands(new ArrayList<>(batch));
        req.setRequestInfo(new org.egov.common.contract.request.RequestInfo());
        repo.save(req);

        BigDecimal released = jt.queryForObject(
                "SELECT COALESCE(SUM(collection_amount),0) FROM eg_emarket_fi_report "
                + "WHERE gl_code=? AND posting_key='50'", BigDecimal.class, CGST_ADV);
        assertEquals(0, released.compareTo(new BigDecimal("100.00")),
                "three demands sharing a 100.00 advance release exactly 100.00 between them, not 191.70");
    }

    @Test @DisplayName("a part-settled advance nets nothing rather than one-sided")
    void asymmetricSettlementNetsNothing() {
        // The advance ran out mid-demand. egov-apportion-service drains buckets in ascending-amount
        // order, so it settles CGST in part and leaves SGST at zero. Both advance GLs are fully
        // funded here — the asymmetry is in the SETTLEMENT, not the balance, which is why a gate on
        // the settled amounts (or on the caps) misses it and posts CGST-only netting legs.
        advance("5000000284rf", "1000.00");
        collection("5000000284rf", "40", SGST_ADV, "1000.00");

        Demand d = saveable("5000000284rf", "D-PARTIAL", "63.90");
        d.getDemandDetails().stream()
                .filter(x -> "CGST".equals(x.getTaxHeadMasterCode()))
                .forEach(x -> x.setCollectionAmount(new BigDecimal("39.80")));
        DemandDetail sgst = detail(d, "SGST", "63.90", "0.00", "350200422");
        sgst.setId(UUID.randomUUID().toString()); sgst.setTenantId("mh.bmc");
        sgst.setDemandId(d.getId()); sgst.setAuditDetails(d.getAuditDetails());
        d.getDemandDetails().add(sgst);

        DemandRequest req = new DemandRequest();
        req.setDemands(new ArrayList<>(List.of(d)));
        req.setRequestInfo(new org.egov.common.contract.request.RequestInfo());
        repo.save(req);

        BigDecimal relCgst = jt.queryForObject("SELECT COALESCE(SUM(collection_amount),0) FROM eg_emarket_fi_report "
                + "WHERE transaction_number='D-PARTIAL' AND gl_code=? AND posting_key='50'", BigDecimal.class, CGST_ADV);
        BigDecimal relSgst = jt.queryForObject("SELECT COALESCE(SUM(collection_amount),0) FROM eg_emarket_fi_report "
                + "WHERE transaction_number='D-PARTIAL' AND gl_code=? AND posting_key='50'", BigDecimal.class, SGST_ADV);
        assertEquals(0, relCgst.compareTo(relSgst),
                "a one-sided 39.80 CGST release would file a GSTR-1 whose halves disagree");
        assertEquals(0, relCgst.compareTo(BigDecimal.ZERO),
                "with SGST unsettled, neither half is released; the advance stays on the register");

        BigDecimal dr = jt.queryForObject("SELECT COALESCE(SUM(collection_amount),0) FROM eg_emarket_fi_report "
                + "WHERE transaction_number='D-PARTIAL' AND posting_key='40'", BigDecimal.class);
        BigDecimal cr = jt.queryForObject("SELECT COALESCE(SUM(collection_amount),0) FROM eg_emarket_fi_report "
                + "WHERE transaction_number='D-PARTIAL' AND posting_key='50'", BigDecimal.class);
        assertEquals(0, dr.compareTo(cr), "the voucher still balances");
    }

    @Test @DisplayName("CGST and SGST always net by the same amount")
    void nettingIsSymmetric() {
        // Only a CGST advance was ever posted (a one-sided legacy row). Releasing CGST against
        // 0.00 of SGST would file a GSTR-1 whose two halves disagree.
        // advance() posts CGST only, so the SGST advance balance is 0.00 by construction.
        advance("5000000284rf", "100.00");

        Demand d = saveable("5000000284rf", "D-SYM", "63.90");
        DemandDetail sgst = detail(d, "SGST", "63.90", "63.90", "350200422");
        sgst.setId(UUID.randomUUID().toString()); sgst.setTenantId("mh.bmc");
        sgst.setDemandId(d.getId()); sgst.setAuditDetails(d.getAuditDetails());
        d.getDemandDetails().add(sgst);

        DemandRequest req = new DemandRequest();
        req.setDemands(new ArrayList<>(List.of(d)));
        req.setRequestInfo(new org.egov.common.contract.request.RequestInfo());
        repo.save(req);

        BigDecimal relCgst = jt.queryForObject("SELECT COALESCE(SUM(collection_amount),0) FROM eg_emarket_fi_report "
                + "WHERE transaction_number='D-SYM' AND gl_code=? AND posting_key='50'", BigDecimal.class, CGST_ADV);
        BigDecimal relSgst = jt.queryForObject("SELECT COALESCE(SUM(collection_amount),0) FROM eg_emarket_fi_report "
                + "WHERE transaction_number='D-SYM' AND gl_code=? AND posting_key='50'", BigDecimal.class, SGST_ADV);
        assertEquals(0, relCgst.compareTo(relSgst),
                "the two halves of an intra-state levy must be released by equal amounts");
    }

    // ------------------------------------------------- licence-level keying

    @Test @DisplayName("the advance is found whatever business-service code it was stamped with")
    void advanceIsHeldAtLicenceLevel() throws Exception {
        // one receipt covering rent AND licence fee stamps the voucher with whichever demand was oldest
        advance("5000000284lf", "766.80");
        assertEquals(0, netOff(demand("5000000284rf", "63.90", "63.90")).compareTo(new BigDecimal("63.90")),
                "a rent demand finds an advance stamped under the licence-fee code");

        Map<String, BigDecimal> batch = new HashMap<>();
        BigDecimal a = capped(demand("5000000284rf", "63.90", "63.90"), batch);
        BigDecimal b = capped(demand("5000000284lf", "63.90", "63.90"), batch);
        assertEquals(0, a.add(b).compareTo(new BigDecimal("127.80")),
                "both service codes draw on the one licensee pool");

        assertEquals(0, netOff(demand("5000000999rf", "63.90", "63.90")).compareTo(BigDecimal.ZERO),
                "a different licence sees none of it");
    }

    // ------------------------------------------------------------- reversal

    @Test @DisplayName("a net-off is reversed once and only once")
    void reversalIsResidualBased() {
        posted("D1", "50", CGST_ADV, "63.90", "upmktdemdadv");
        posted("D1", "40", CGST_PAY, "63.90", "upmktdemdadv");

        List<FiReport> first = repo.buildGstNettingReversalFiReports("D1", null);
        assertEquals(2, first.size(), "the standing net-off is reversed");
        first.forEach(r -> posted("D1", r.getPostingKey(), r.getGlCode(),
                r.getCollectionAmount().toPlainString(), "upmktdemdrev"));

        assertTrue(repo.buildGstNettingReversalFiReports("D1", null).isEmpty(),
                "the second reversal path finds nothing left and must not reverse again");
        assertEquals(0, signed("eg_emarket_fi_report", CGST_ADV).compareTo(BigDecimal.ZERO),
                "the advance asset is square, with no phantom balance for later demands to net against");
    }

    @Test @DisplayName("cancelling an advance unwinds every control account")
    void cancellationUnwindsFully() {
        // advance receipt
        for (String[] r : new String[][]{ {"40","450100100","10056.00"}, {"50",ADVANCE,"10056.00"},
                {"50",CGST_PAY,"766.80"}, {"40",CGST_ADV,"766.80"} })
            collection("5000000284rf", r[0], r[1], r[2]);
        // one demand settled from it
        posted("D1", "50", "130100301", "710.00", "upmktdemdadv");
        posted("D1", "50", CGST_PAY, "63.90", "upmktdemdadv");
        posted("D1", "40", CGST_PAY, "63.90", "upmktdemdadv");
        posted("D1", "50", CGST_ADV, "63.90", "upmktdemdadv");
        posted("D1", "40", ADVANCE, "838.00", "upmktdemdadv");
        // the receipt is cancelled: collection legs mirrored...
        for (String[] r : new String[][]{ {"50","450100100","10056.00"}, {"40",ADVANCE,"10056.00"},
                {"40",CGST_PAY,"766.80"}, {"50",CGST_ADV,"766.80"} })
            collection("5000000284rf", r[0], r[1], r[2]);
        // ...and the demand side unwound
        List<FiReport> comp = new ArrayList<>();
        comp.addAll(repo.buildGstNettingReversalFiReports("D1", null));
        comp.addAll(repo.buildAdvanceSettlementReversalFiReports("D1"));
        comp.forEach(r -> posted("D1", r.getPostingKey(), r.getGlCode(),
                r.getCollectionAmount().toPlainString(), "upmktdemdrev"));

        assertEquals(0, both(ADVANCE).compareTo(BigDecimal.ZERO), "advance liability unwound");
        assertEquals(0, both(CGST_ADV).compareTo(BigDecimal.ZERO), "GST advance asset unwound");
        assertEquals(0, both("450100100").compareTo(BigDecimal.ZERO), "bank unwound");
        assertEquals(0, both(RECEIVABLE).compareTo(new BigDecimal("-838.00")),
                "the re-opened dues are back on the receivable");
        assertTrue(repo.buildGstNettingReversalFiReports("D1", null).isEmpty()
                && repo.buildAdvanceSettlementReversalFiReports("D1").isEmpty(),
                "running the unwind again is a no-op");
    }

    // ----------------------------------------------------- end to end, real save()

    @Test @DisplayName("save() writes a balanced demand-against-advance voucher")
    void endToEndThroughSave() {
        for (String[] r : new String[][]{ {"40",CGST_ADV,"766.80"}, {"40",SGST_ADV,"766.80"} })
            collection("5000000284rf", r[0], r[1], r[2]);
        jt.update("INSERT INTO eg_emarket_demand_settlement_info "
                + "(advance_demandid,settled_demandid,consumercode,periodfrom,periodto) VALUES ('ADV','D-E2E',?,1,2)",
                "5000000284rf");

        Demand d = demand("5000000284rf", "63.90", "63.90");
        d.setId("D-E2E");
        d.setTenantId("mh.bmc"); d.setConsumerType("market");
        d.setStatus(Demand.StatusEnum.ACTIVE); d.setMinimumAmountPayable(BigDecimal.ZERO);
        d.setTaxPeriodTo(d.getTaxPeriodFrom() + 2678400000L);
        d.setAuditDetails(AuditDetails.builder().createdBy("u").createdTime(1L)
                .lastModifiedBy("u").lastModifiedTime(1L).build());
        Map<String, String> add = new HashMap<>();
        add.put("fund", "11"); add.put("fundCenter", "4060420103");
        add.put("businessArea", "4060"); add.put("functionalArea", "55800000000");
        d.setAdditionalDetails(add);
        d.getDemandDetails().add(detail(d, "STALLAGE", "710.00", "710.00", "130100301"));
        d.getDemandDetails().forEach(dd -> { dd.setId(UUID.randomUUID().toString()); dd.setTenantId("mh.bmc");
            dd.setDemandId(d.getId()); dd.setAuditDetails(d.getAuditDetails()); });

        DemandRequest req = new DemandRequest();
        req.setDemands(new ArrayList<>(List.of(d)));
        req.setRequestInfo(new org.egov.common.contract.request.RequestInfo());
        repo.save(req);

        List<Map<String, Object>> fi = jt.queryForList(
                "SELECT posting_key, gl_code, collection_amount FROM eg_emarket_fi_report WHERE transaction_number='D-E2E'");
        BigDecimal dr = BigDecimal.ZERO, cr = BigDecimal.ZERO;
        for (Map<String, Object> r : fi) {
            BigDecimal amt = (BigDecimal) r.get("collection_amount");
            if ("40".equals(r.get("posting_key"))) dr = dr.add(amt); else cr = cr.add(amt);
        }
        assertEquals(0, dr.compareTo(cr), "the voucher balances");
        assertTrue(fi.stream().anyMatch(r -> CGST_ADV.equals(r.get("gl_code")) && "50".equals(r.get("posting_key"))),
                "the net-off releases the GST advance");
        assertTrue(fi.stream().anyMatch(r -> ADVANCE.equals(r.get("gl_code")) && "40".equals(r.get("posting_key"))),
                "the balancing debit draws down the advance rather than raising a receivable");
        assertTrue(fi.stream().noneMatch(r -> RECEIVABLE.equals(r.get("gl_code"))),
                "nothing is receivable — the licensee already paid");

        assertEquals(0, (int) jt.queryForObject("SELECT count(*) FROM egbs_demanddetail_v1 WHERE demandid='D-E2E' "
                + "AND taxheadcode IN ('CSP40','SSP40','CSA50','SSA50')", Integer.class),
                "the synthetic netting heads stay FI-only and never reach the demand table");
    }

    @Test @DisplayName("a redelivered message cannot release the advance twice")
    void replayIsSafe() {
        endToEndThroughSave();
        BigDecimal before = signed("eg_emarket_fi_report", CGST_ADV);
        int rowsBefore = jt.queryForObject(
                "SELECT count(*) FROM eg_emarket_fi_report WHERE transaction_number='D-E2E'", Integer.class);

        Demand d = demand("5000000284rf", "63.90", "63.90");
        d.setId("D-E2E"); d.setTenantId("mh.bmc");
        DemandRequest req = new DemandRequest();
        req.setDemands(new ArrayList<>(List.of(d)));
        req.setRequestInfo(new org.egov.common.contract.request.RequestInfo());
        assertThrows(Exception.class, () -> repo.save(req), "the duplicate is rejected, not silently re-applied");

        assertEquals(0, signed("eg_emarket_fi_report", CGST_ADV).compareTo(before), "no further advance released");
        assertEquals(rowsBefore, (int) jt.queryForObject(
                "SELECT count(*) FROM eg_emarket_fi_report WHERE transaction_number='D-E2E'", Integer.class),
                "no duplicate FI rows");
    }

    // ------------------------------------------------------------- helpers

    private BigDecimal netOff(Demand d) throws Exception { return capped(d, new HashMap<>()); }

    @SuppressWarnings("unchecked")
    private BigDecimal capped(Demand d, Map<String, BigDecimal> batch) throws Exception {
        Method m = DemandRepository.class.getDeclaredMethod("cappedGstNetOff",
                Demand.class, String.class, String.class, Map.class);
        m.setAccessible(true);
        return (BigDecimal) m.invoke(repo, d, "CGST", CGST_ADV, batch);
    }

    private Demand demand(String consumerCode, String tax, String collected) {
        Demand d = Demand.builder().id("D-" + UUID.randomUUID()).consumerCode(consumerCode)
                .businessService("TX.Emarket_Rental_Fees").taxPeriodFrom(1785522600000L).build();
        d.setDemandDetails(new ArrayList<>(List.of(detail(d, "CGST", tax, collected, CGST_PAY))));
        d.setApportionedAgainstAdvance(true);
        return d;
    }

    private DemandDetail detail(Demand d, String head, String tax, String collected, String gl) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("glcode", gl);
        return DemandDetail.builder().taxHeadMasterCode(head).taxAmount(new BigDecimal(tax))
                .collectionAmount(new BigDecimal(collected)).additionalDetails(extra).build();
    }

    /** A demand complete enough for the real save() path: ids, tenant, status and audit set. */
    private Demand saveable(String consumerCode, String id, String cgst) {
        return saveable(consumerCode, id, cgst, 0);
    }

    /**
     * @param monthIndex shifts the tax period. egbs_demand_v1 is unique on
     *        (consumercode, tenantid, taxperiodfrom, taxperiodto, businessservice), so several
     *        demands for one licensee in a single batch must sit in different months — which is
     *        exactly how RestorationService.unblock raises them.
     */
    private Demand saveable(String consumerCode, String id, String cgst, int monthIndex) {
        Demand d = demand(consumerCode, cgst, cgst);
        d.setId(id);
        d.setTaxPeriodFrom(d.getTaxPeriodFrom() + monthIndex * 2678400000L);
        d.setTenantId("mh.bmc");
        d.setConsumerType("market");
        d.setStatus(Demand.StatusEnum.ACTIVE);
        d.setMinimumAmountPayable(BigDecimal.ZERO);
        d.setTaxPeriodTo(d.getTaxPeriodFrom() + 2678400000L);
        d.setAuditDetails(AuditDetails.builder().createdBy("u").createdTime(1L)
                .lastModifiedBy("u").lastModifiedTime(1L).build());
        Map<String, String> add = new HashMap<>();
        add.put("fund", "11"); add.put("fundCenter", "4060420103");
        add.put("businessArea", "4060"); add.put("functionalArea", "55800000000");
        d.setAdditionalDetails(add);
        d.getDemandDetails().forEach(dd -> { dd.setId(UUID.randomUUID().toString());
            dd.setTenantId("mh.bmc"); dd.setDemandId(d.getId()); dd.setAuditDetails(d.getAuditDetails()); });
        jt.update("INSERT INTO eg_emarket_demand_settlement_info "
                + "(advance_demandid,settled_demandid,consumercode,periodfrom,periodto) VALUES ('ADV',?,?,1,2)",
                id, consumerCode);
        return d;
    }

    private void advance(String consumerCode, String amount) { collection(consumerCode, "40", CGST_ADV, amount); }

    private void collection(String consumerCode, String key, String gl, String amount) {
        jt.update("INSERT INTO eg_emarket_fi_report_collection (transaction_number,reference_no,"
                + "document_header_text,posting_key,gl_code,collection_amount,report_type,doc_date,created_at,updated_at) "
                + "VALUES ('ADV',?,'rcpt',?,?,?::numeric,'upmktcoll',1785522600000,now(),now())",
                consumerCode, key, gl, amount);
    }

    private void posted(String demandId, String key, String gl, String amount, String type) {
        jt.update("INSERT INTO eg_emarket_fi_report (transaction_number,reference_no,document_header_text,"
                + "posting_key,gl_code,collection_amount,report_type,doc_date,created_at,updated_at) "
                + "VALUES (?, '5000000284rf','1',?,?,?::numeric,?,1785522600000,now(),now())",
                demandId, key, gl, amount, type);
    }

    private BigDecimal signed(String table, String gl) {
        return jt.queryForObject("SELECT COALESCE(SUM(CASE WHEN posting_key='50' THEN collection_amount "
                + "ELSE -collection_amount END),0) FROM " + table + " WHERE gl_code=?", BigDecimal.class, gl);
    }

    private BigDecimal both(String gl) {
        return signed("eg_emarket_fi_report_collection", gl).add(signed("eg_emarket_fi_report", gl));
    }

    private void clearFi() {
        jt.update("DELETE FROM eg_emarket_fi_report_collection");
        jt.update("DELETE FROM eg_emarket_fi_report");
    }

    private static void set(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
}
