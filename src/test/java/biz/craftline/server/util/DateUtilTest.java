package biz.craftline.server.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilTest {

    private final DateUtil dateUtil = new DateUtil();

    @Test
    void formatDateTime_handlesNull() {
        assertNull(dateUtil.formatDateTime((Date) null));
        assertNull(dateUtil.formatDateTime((LocalDateTime) null));
    }

    @Test
    void formatDateTime_formatsValues() {
        Date date = dateUtil.parseDate("2024-01-15");
        assertNotNull(dateUtil.formatDateTime(date));
        assertEquals("2024-01-15", dateUtil.formatYMD(date));
        LocalDateTime ldt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        assertNotNull(dateUtil.formatDateTime(ldt));
    }

    @Test
    void parseDateTime_successAndFailure() {
        assertNotNull(dateUtil.parseDateTime("2024-01-15T10:30:00"));
        assertNull(dateUtil.parseDateTime(null));
        assertThrows(RuntimeException.class, () -> dateUtil.parseDateTime("bad"));
    }

    @Test
    void parseLocalDateTime_successAndFailure() {
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 30, 0),
                dateUtil.parseLocalDateTime("2024-01-15 10:30:00"));
        assertNull(dateUtil.parseLocalDateTime(null));
        assertThrows(RuntimeException.class, () -> dateUtil.parseLocalDateTime("bad"));
    }

    @Test
    void parseDate_successAndFailure() {
        assertNotNull(dateUtil.parseDate("2024-01-15"));
        assertNull(dateUtil.parseDate(null));
        assertThrows(RuntimeException.class, () -> dateUtil.parseDate("not-a-date"));
    }
}
