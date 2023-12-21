package sn.intouch.gu.lonaciapi.ejb.bigquery.services;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Local
public interface BigQueryService {
    List<Map<String, String>> getAggregation(Date startDate, Date endDate, String operator, String type);
}
