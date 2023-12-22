package sn.intouch.gu.lonaciapi.ejb.utils;

import sn.intouch.gu.lonaciapi.ejb.bigquery.enums.AggregationTimeEnum;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

public class DateUtil {
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("UTC");

    public static LocalDateTime startOfDay() {
        return LocalDateTime.now(DEFAULT_ZONE_ID).with(LocalTime.MIN);
    }

    public static LocalDateTime endOfDay() {
        // return LocalDateTime.now(DEFAULT_ZONE_ID).with(LocalTime.MAX);
        return LocalDateTime.now(DEFAULT_ZONE_ID).with(LocalTime.MIN).plusDays(1);
    }


    //note that week starts with Monday
    public static LocalDateTime startOfWeek() {
        return LocalDateTime.now(DEFAULT_ZONE_ID)
                .with(LocalTime.MIN)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    //note that week ends with Sunday
    public static LocalDateTime endOfWeek() {
        return LocalDateTime.now(DEFAULT_ZONE_ID)
                .with(LocalTime.MAX)
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    public static LocalDateTime startOfMonth() {
        return LocalDateTime.now(DEFAULT_ZONE_ID)
                .with(LocalTime.MIN)
                .with(TemporalAdjusters.firstDayOfMonth());
    }

    public static LocalDateTime endOfMonth() {
        return LocalDateTime.now(DEFAULT_ZONE_ID)
                .with(LocalTime.MAX)
                .with(TemporalAdjusters.lastDayOfMonth());
    }


    public static long toMills(final LocalDateTime localDateTime) {
        return localDateTime.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli();
    }

    public static Date toDate(final LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(DEFAULT_ZONE_ID).toInstant());
    }

    public static String toString(final LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public static Date getStartDateFromDateString(AggregationTimeEnum timeEnum) {;
        if (timeEnum.equals(AggregationTimeEnum.DAY))
            return Date.from(DateUtil.startOfDay().toInstant(ZoneOffset.UTC));
        else if(timeEnum.equals(AggregationTimeEnum.WEEK))
            return Date.from(DateUtil.startOfWeek().toInstant(ZoneOffset.UTC));
        else if (timeEnum.equals(AggregationTimeEnum.MONTH))
            return Date.from(DateUtil.startOfMonth().toInstant(ZoneOffset.UTC));
        return null;
    }

    public static Date getEndOfDay() {
        return Date.from(DateUtil.endOfDay().toInstant(ZoneOffset.UTC));
    }
}