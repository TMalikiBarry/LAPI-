package sn.intouch.gu.lonaciapi.ejb.utils;

import lombok.extern.log4j.Log4j2;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Log4j2
public class Utils {
    public static String formatLabelAmount(Double amount) {
        NumberFormat format;
        if (amount == null)
            return "0";

        format = NumberFormat.getIntegerInstance();
        format.setGroupingUsed(false);
        format.setMaximumFractionDigits(2);
        return format.format(amount);
    }

    public static Date toDate(String dateTimeStr) {
        log.info("Parsing date : {}", dateTimeStr);
        LocalDateTime ldt = LocalDateTime.parse(dateTimeStr);
        ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
        Instant instant = zdt.toInstant();
        return Date.from(instant);
    }

    public static Date addOneHour(Date date) {
        Instant instant = date.toInstant().plus(1, ChronoUnit.HOURS);
        return Date.from(instant);
    }

    public static void main(String[] args) {
        String input = "2025-11-22T01:00:00";
        Date date = toDate(input);
        System.out.println(date);
    }
}
