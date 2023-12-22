package sn.intouch.gu.lonaciapi.ejb.bigquery.services;

import sn.intouch.gu.lonaciapi.ejb.bigquery.enums.AggregationTimeEnum;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Local
public interface BigQueryService {
    List<Map<String, String>> getAggregation(Date startDate, Date endDate, AggregationTimeEnum time, String operator, String type);

    Map<String, String> getSumBetweenDates(Date startDate, Date endDate, String operatorId, String type);

}
