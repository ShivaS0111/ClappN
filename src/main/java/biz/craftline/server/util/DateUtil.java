package biz.craftline.server.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

@Slf4j
@Component
public class DateUtil {

    private static final SimpleDateFormat DATE_FORMATTER_YMDHMS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final SimpleDateFormat DATE_FORMATTER_YMD = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateTimeFormatter LOCALDATETIME_FORMATTER_YMDHMS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String formatDateTime(Date dateTime) {
        return dateTime != null ? DATE_FORMATTER_YMDHMS.format(dateTime) : null;
    }

    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? DATE_FORMATTER_YMDHMS.format(dateTime) : null;
    }

    public String formatYMD(Date dateTime) {
        return dateTime != null ? DATE_FORMATTER_YMD.format(dateTime) : null;
    }

    public Date parseDateTime(String dateTimeStr) {
        try {
            return dateTimeStr != null ? DATE_FORMATTER_YMDHMS.parse(dateTimeStr) : null;
        } catch (ParseException e) {
            log.info("parseDateTime: Invalid date format: " + dateTimeStr, e);
            throw new RuntimeException("Invalid date format2: " + dateTimeStr, e);
        }
    }

    public LocalDateTime parseLocalDateTime(String dateTimeStr) {
        try {
            return dateTimeStr != null ? LocalDateTime.parse(dateTimeStr, LOCALDATETIME_FORMATTER_YMDHMS) : null;
        } catch (DateTimeParseException e) {
            log.info("parseDateTime: Invalid date format: " + dateTimeStr, e);
            throw new RuntimeException("Invalid date format2: " + dateTimeStr, e);
        }
    }

    public Date parseDate(String dateStr) {
        try {
            return dateStr != null ? DATE_FORMATTER_YMD.parse(dateStr) : null;
        } catch (ParseException e) {
            log.info("parseDate: Invalid date format: " + dateStr, e);
            throw new RuntimeException("Invalid date format: " + dateStr, e);
        }
    }

}
