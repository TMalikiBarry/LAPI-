package sn.intouch.gu.lonaciapi.ejb.bigquery.services;

import sn.intouch.gu.lonaciapi.ejb.bigquery.enums.AggregationTimeEnum;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Local
public interface BigQueryService {

    List<Map<String, String>> getAggregation(Date startDate, Date endDate, AggregationTimeEnum time, String operator, String type, boolean formatDateGrouper,
                                             Boolean computeVolume, String category);

    @Deprecated
    Map<String, String> getSumBetweenDates(Date startDate, Date endDate, String operatorId, String type);

    Map<String, String> getSumBetweenDatesV2(Date startDate, Date endDate, String operator, String type, Boolean computeVolume, String category);

    Map<String, String> getSumBetweenDatesAllCategories(Date startDate, Date endDate, String operator, String type,
                                                        Boolean computeVolume);

    Integer getActiveClients(Date startDate, Date endDate, String operator, String type);

    List<Map<String, String>> getSumClientsBetweenDates(Date startDate, Date endDate, String operator, String type);

    Map<String, String> getSumBetweenDatesWithCategoryAndUseToCompute(Date startDate, Date endDate, String operator, String category, Boolean useToCompute);
}
