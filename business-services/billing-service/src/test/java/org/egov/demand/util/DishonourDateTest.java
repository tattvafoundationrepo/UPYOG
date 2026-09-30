package org.egov.demand.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DishonourDateTest {

    @Test
    public void aDayBecomesMidnightUtcAndReadsAsTheSameDayInIst() {
        Long ms = DishonourDate.toEpochMillis("2026-09-25");
        assertEquals(Long.valueOf(1790294400000L), ms);
        assertEquals("2026-09-25", Instant.ofEpochMilli(ms).atZone(ZoneId.of("Asia/Kolkata")).toLocalDate().toString());
        assertEquals("2026-09-25", Instant.ofEpochMilli(ms).atZone(ZoneId.of("UTC")).toLocalDate().toString());
    }

    @Test
    public void boundaryDays() {
        assertEquals("2026-03-31", day(DishonourDate.toEpochMillis("2026-03-31")));
        assertEquals("2026-04-01", day(DishonourDate.toEpochMillis("2026-04-01")));
        assertEquals("2028-02-29", day(DishonourDate.toEpochMillis("2028-02-29")));
        assertEquals("2026-12-31", day(DishonourDate.toEpochMillis(" 2026-12-31 ")));
    }

    @Test
    public void invalidOrEmptyIsNull() {
        for (String bad : new String[] { null, "", "  ", "2026-02-29", "2026-02-30", "25-09-2026", "2026/09/25", "abc", "2026-9-5" }) {
            assertNull(DishonourDate.toEpochMillis(bad), String.valueOf(bad));
        }
    }

    @Test
    public void readsMapsAndJsonNodes() {
        Map<String, Object> map = new HashMap<>();
        map.put("dishonourDate", "2026-09-25");
        assertEquals(Long.valueOf(1790294400000L), DishonourDate.fromAdditionalDetails(map));
        ObjectNode node = JsonNodeFactory.instance.objectNode().put("dishonourDate", "2026-09-25");
        assertEquals(Long.valueOf(1790294400000L), DishonourDate.fromAdditionalDetails(node));
        ObjectNode nullNode = JsonNodeFactory.instance.objectNode();
        nullNode.putNull("dishonourDate");
        assertNull(DishonourDate.fromAdditionalDetails(nullNode));
        assertNull(DishonourDate.fromAdditionalDetails(Collections.emptyMap()));
        assertNull(DishonourDate.fromAdditionalDetails(null));
        assertNull(DishonourDate.fromAdditionalDetails("2026-09-25"));
        assertNull(DishonourDate.fromAdditionalDetails(JsonNodeFactory.instance.nullNode()));
    }

    private static String day(Long ms) {
        return Instant.ofEpochMilli(ms).atZone(ZoneId.of("UTC")).toLocalDate().toString();
    }
}
