package sn.intouch.gu.lonaciapi.ejb.bigquery.services;

import sn.intouch.gu.lonaciapi.ejb.bigquery.enums.AggregationTimeEnum;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Local
public interface BigQueryService {

    List<Map<String, String>> getAggregation(Date startDate, Date endDate, AggregationTimeEnum time, String operator, String type, boolean formatDateGrouper,
                                             Boolean computeVolume, String category, String country);

    @Deprecated
    Map<String, String> getSumBetweenDates(Date startDate, Date endDate, String operatorId, String type, String country);

    Map<String, String> getSumBetweenDatesV2(Date startDate, Date endDate, String operator, String type, Boolean computeVolume, String category, String country);

    Map<String, String> getSumBetweenDatesAllCategories(Date startDate, Date endDate, String operator, String type,
                                                        Boolean computeVolume, String country);

    List<Map<String, String>> getSumBetweenDatesAllCategories(Date startDate, Date endDate, Set<String> operators, String type,
                                                              Boolean computeVolume);

    Integer getActiveClients(Date startDate, Date endDate, String operator, String type, String country);

    List<Map<String, String>> getSumClientsBetweenDates(Date startDate, Date endDate, String operator, String type);

    Map<String, String> getSumBetweenDatesWithCategoryAndUseToCompute(Date startDate, Date endDate, String operator, String category, Boolean useToCompute);
}
