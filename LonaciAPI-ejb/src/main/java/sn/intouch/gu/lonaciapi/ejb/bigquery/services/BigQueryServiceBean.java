package sn.intouch.gu.lonaciapi.ejb.bigquery.services;

import com.google.cloud.bigquery.*;
import lombok.extern.log4j.Log4j2;
import sn.intouch.gu.lonaciapi.ejb.bigquery.BigQueryConnection;
import sn.intouch.gu.lonaciapi.ejb.bigquery.enums.AggregationTimeEnum;
import sn.intouch.gu.lonaciapi.ejb.utils.Utils;

import javax.ejb.Stateless;
import java.util.*;

import static sn.intouch.gu.lonaciapi.ejb.utils.DateUtil.SIMPLE_DATE_FORMAT;
import static sn.intouch.gu.lonaciapi.ejb.utils.DateUtil.SIMPLE_DATE_FORMAT_WITH_HOUR;

@Stateless
@Log4j2
public class BigQueryServiceBean implements BigQueryService{

    @Override
    public List<Map<String, String>> getAggregation(Date startDate, Date endDate, AggregationTimeEnum time, String operator, String type, boolean formatDateGrouper,
                                                    Boolean computeVolume, String category, String country) {
        BigQueryConnection connection = new BigQueryConnection();
        System.out.println("START DATE :: " + startDate + " END DATE :: " + endDate);
        try {
            String grouper = formatDateGrouper ? this.getFormattedGrouper(time, "date") : getGrouper(time, "date");
            String query = "SELECT " + grouper + " ddate, COUNT(*) as number, SUM( CASE WHEN type.direction = 'DEBIT' then - trx.montant ELSE trx.montant END ) as sum" +
                    " FROM " + connection.getLonaciTableRef() + " trx LEFT JOIN " + connection.getLonaciTypeTableRef() + " type ON trx.type_transaction = type.code "
                    + " WHERE trx.date BETWEEN @startDate AND @endDate ";
            if (operator != null)
                query += " AND trx.operateur_id = @operator";
            if (type != null)
                query += " AND trx.type_transaction = @type";
            if (country != null)
                query += " AND trx.country = @country ";
            if (computeVolume != null)
                query += " AND type.use_to_compute_volume = @computeVolume";
            if (category != null)
                query += " AND type.category = @category ";

            query += " GROUP BY ddate ORDER BY ddate ASC;";

            QueryJobConfiguration.Builder queryConfig = QueryJobConfiguration.newBuilder(query);
            if (formatDateGrouper) {
                queryConfig.addNamedParameter("startDate", QueryParameterValue.dateTime(SIMPLE_DATE_FORMAT_WITH_HOUR.format(startDate)))
                        .addNamedParameter("endDate", QueryParameterValue.dateTime(SIMPLE_DATE_FORMAT_WITH_HOUR.format(endDate)));
            }
            if (operator != null)
                queryConfig.addNamedParameter("operator", QueryParameterValue.string(operator));
            if (country != null)
                queryConfig.addNamedParameter("country", QueryParameterValue.string(country));
            if (type != null)
                queryConfig.addNamedParameter("type", QueryParameterValue.string(type));
            if (computeVolume != null)
                queryConfig.addNamedParameter("computeVolume", QueryParameterValue.int64((Boolean.TRUE.equals(computeVolume) ? 1 : 0)));
            if (category != null)
                queryConfig.addNamedParameter("category", QueryParameterValue.string(category));

            BigQuery bigquery = connection.getConnection();
            TableResult result = bigquery.query(queryConfig.build());

            List<Map<String, String>> responses = new ArrayList<>();
            Schema schema = result.getSchema();
            for (FieldValueList row : result.iterateAll()) {
                Map<String, String> m = new HashMap<>();
                for (Field field : schema.getFields()) {
                    String value = this.getStringValue(field.getName(), row.get(field.getName()));
                    if(field.getName().equals("sum"))
                        m.put(field.getName(), Utils.formatLabelAmount(Double.valueOf(value)));
                    else
                        m.put(field.getName(), value);
                }
                responses.add(m);
            }
            return responses;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    @Override
    @Deprecated
    public Map<String, String> getSumBetweenDates(Date startDate, Date endDate, String operator, String type, String country) {
        try {
            BigQueryConnection connection = new BigQueryConnection();
            String query = "SELECT COUNT(*) as number, SUM(trx.montant) as sum FROM "+ connection.getLonaciTableRef() +" trx "
                    + " WHERE trx.date BETWEEN @startDate AND @endDate ";
            if (operator != null)
                query += " AND operateur_id = @operator";
            if (country != null)
                query += " AND country = @country";
            if (type != null)
                query += " AND type_transaction = @type";

            QueryJobConfiguration.Builder queryConfig = QueryJobConfiguration.newBuilder(query)
                    .addNamedParameter("startDate", QueryParameterValue.date(SIMPLE_DATE_FORMAT.format(startDate)))
                    .addNamedParameter("endDate", QueryParameterValue.date(SIMPLE_DATE_FORMAT.format(endDate)));

            if (operator != null)
                queryConfig.addNamedParameter("operator", QueryParameterValue.string(operator));
            if (country != null)
                queryConfig.addNamedParameter("country", QueryParameterValue.string(country));
            if (type != null)
                queryConfig.addNamedParameter("type", QueryParameterValue.string(type));

            BigQuery bigquery = connection.getConnection();
            TableResult result = bigquery.query(queryConfig.build());

            Schema schema = result.getSchema();
            Map<String, String> m = new HashMap<>();
            for (FieldValueList row : result.iterateAll()) {
                for (Field field : schema.getFields()) {
                    if(field.getName().equals("sum"))
                        m.put(field.getName(), Utils.formatLabelAmount(Double.valueOf(row.get(field.getName()).getStringValue())));
                    else
                        m.put(field.getName(), row.get(field.getName()).getStringValue());
                }
                break;
            }
            return m;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }
    @Override
    public Map<String, String> getSumBetweenDatesV2(Date startDate, Date endDate, String operator, String type,
                                                    Boolean computeVolume, String category, String country) {
        try {
            BigQueryConnection connection = new BigQueryConnection();
            String query = "SELECT COUNT(*) as number, SUM( CASE WHEN type.direction = 'DEBIT' then - trx.montant ELSE trx.montant END ) as sum " +
                    "FROM "+ connection.getLonaciTableRef() +" trx LEFT JOIN " + connection.getLonaciTypeTableRef() + " type ON trx.type_transaction = type.code "
                    + " WHERE trx.date BETWEEN @startDate AND @endDate ";
            if (operator != null)
                query += " AND operateur_id = @operator";
            if (country != null)
                query += " AND country = @country";
            if (type != null)
                query += " AND type_transaction = @type";
            if (computeVolume != null)
                query += " AND type.use_to_compute_volume = @computeVolume";
            if (category != null)
                query += " AND type.category = @category ";

            QueryJobConfiguration.Builder queryConfig = QueryJobConfiguration.newBuilder(query)
                    .addNamedParameter("startDate", QueryParameterValue.dateTime(SIMPLE_DATE_FORMAT_WITH_HOUR.format(startDate)))
                    .addNamedParameter("endDate", QueryParameterValue.dateTime(SIMPLE_DATE_FORMAT_WITH_HOUR.format(endDate)));

            if (operator != null)
                queryConfig.addNamedParameter("operator", QueryParameterValue.string(operator));
            if (country != null)
                queryConfig.addNamedParameter("country", QueryParameterValue.string(country));
            if (type != null)
                queryConfig.addNamedParameter("type", QueryParameterValue.string(type));
            if (computeVolume != null)
                queryConfig.addNamedParameter("computeVolume", QueryParameterValue.int64((Boolean.TRUE.equals(computeVolume) ? 1 : 0)));
            if (category != null)
                queryConfig.addNamedParameter("category", QueryParameterValue.string(category));

            BigQuery bigquery = connection.getConnection();
            TableResult result = bigquery.query(queryConfig.build());

            Schema schema = result.getSchema();
            Map<String, String> m = new HashMap<>();
            for (FieldValueList row : result.iterateAll()) {
                for (Field field : schema.getFields()) {
                    String value = this.getStringValue(field.getName(), row.get(field.getName()));
                    m.put(field.getName(), value);
                }
                break;
            }
            return m;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }
    @Override
    public Map<String, String> getSumBetweenDatesAllCategories(Date startDate, Date endDate, String operator, String type,
                                                               Boolean computeVolume, String country) {
        try {
            BigQueryConnection connection = new BigQueryConnection();
            String query = "SELECT  " +
                    "  SUM( " +
                    "    CASE WHEN type.category = 'BONUS' " +
                    "    THEN " +
                    "      CASE WHEN type.direction = 'CREDIT' " +
                    "      then trx.montant  " +
                    "      ELSE - trx.montant  " +
                    "      END " +
                    "    ELSE 0 " +
                    "    END " +
                    "  ) as bonus, " +
                    "  SUM( " +
                    "    CASE WHEN type.category = 'MISES' " +
                    "    THEN " +
                    "      CASE WHEN type.direction = 'CREDIT' " +
                    "      then trx.montant  " +
                    "      ELSE - trx.montant  " +
                    "      END " +
                    "    ELSE 0 " +
                    "    END " +
                    "  ) as mises, " +
                    "  SUM( " +
                    "    CASE WHEN type.category = 'GAIN' " +
                    "    THEN " +
                    "      CASE WHEN type.direction = 'CREDIT' " +
                    "      then trx.montant  " +
                    "      ELSE - trx.montant  " +
                    "      END " +
                    "    ELSE 0 " +
                    "    END " +
                    "  ) as gain, " +
                    "  SUM( " +
                    "    CASE WHEN type.category = 'PAY_IN' " +
                    "    THEN " +
                    "      CASE WHEN type.direction = 'CREDIT' " +
                    "      then trx.montant  " +
                    "      ELSE - trx.montant  " +
                    "      END " +
                    "    ELSE 0 " +
                    "    END " +
                    "  ) as payin, " +
                    "  SUM( " +
                    "    CASE WHEN type.category = 'PAY_OUT' " +
                    "    THEN " +
                    "      CASE WHEN type.direction = 'CREDIT' " +
                    "      then trx.montant  " +
                    "      ELSE - trx.montant  " +
                    "      END " +
                    "    ELSE 0 " +
                    "    END " +
                    "  ) as payout, " +
                    "  COUNT( " +
                    "    CASE WHEN type.category = 'BONUS' " +
                    "    THEN 1 " +
                    "    END " +
                    "  ) as bonusCount, " +
                    "  COUNT( " +
                    "    CASE WHEN type.category = 'MISES' " +
                    "    THEN 1 " +
                    "    END " +
                    "  ) as misesCount, " +
                    "  COUNT( " +
                    "    CASE WHEN type.category = 'GAIN' " +
                    "    THEN 1 " +
                    "    END " +
                    "  ) as gainCount, " +
                    "  COUNT( " +
                    "    CASE WHEN type.category = 'PAY_IN' " +
                    "    THEN 1 " +
                    "    END " +
                    "  ) as payinCount, " +
                    "  COUNT( " +
                    "    CASE WHEN type.category = 'PAY_OUT' " +
                    "    THEN 1 " +
                    "    END " +
                    "  ) as payoutCount, " +
                    "  SUM( " +
                    "    CASE WHEN type.category = 'GAIN' AND type.direction = 'DEBIT' AND  ABS(trx.montant) >= 500000" +
                    "    THEN   ABS(trx.montant)*0.15  " +
                    "    ELSE 0 " +
                    "    END " +
                    "  ) as withholding " +
                    "  FROM " + connection.getLonaciTableRef() + " trx LEFT JOIN " + connection.getLonaciTypeTableRef() + " type ON trx.type_transaction = type.code " +
                    "  WHERE trx.date BETWEEN @startDate AND @endDate ";

            if (type != null)
                query += " AND trx.type_transaction = @type";
            if (country != null)
                query += " AND trx.country = @country";
            if (computeVolume != null)
                query += " AND type.use_to_compute = @use_to_compute";
            if (operator != null) {
                query += " AND trx.operateur_id = @operator\n";
            }

            if (operator != null && type != null) {
                query += " GROUP BY trx.operateur_id, trx.type ";
            } else if (operator != null) {
                query += " GROUP BY trx.operateur_id ";
            } else if (type != null) {
                query += " GROUP BY trx.type ";
            }
            QueryJobConfiguration.Builder queryConfig = QueryJobConfiguration.newBuilder(query)
                    .addNamedParameter("startDate", QueryParameterValue.dateTime(SIMPLE_DATE_FORMAT_WITH_HOUR.format(startDate)))
                    .addNamedParameter("endDate", QueryParameterValue.dateTime(SIMPLE_DATE_FORMAT_WITH_HOUR.format(endDate)));

            if (operator != null)
                queryConfig.addNamedParameter("operator", QueryParameterValue.string(operator));
            if (country != null)
                queryConfig.addNamedParameter("country", QueryParameterValue.string(country));
            if (type != null)
                queryConfig.addNamedParameter("type", QueryParameterValue.string(type));
            if (computeVolume != null)
                queryConfig.addNamedParameter("use_to_compute", QueryParameterValue.int64((Boolean.TRUE.equals(computeVolume) ? 1 : 0)));

            BigQuery bigquery = connection.getConnection();
            TableResult result = bigquery.query(queryConfig.build());

            Schema schema = result.getSchema();
            Map<String, String> m = new HashMap<>();
            for (FieldValueList row : result.iterateAll()) {
                for (Field field : schema.getFields()) {
                    String value = this.getStringValue(field.getName(), row.get(field.getName()));
                    m.put(field.getName(), value);
                }
                break;
            }
            return m;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    @Override
    public List<Map<String, String>> getSumBetweenDatesAllCategories(Date startDate, Date endDate, Set<String> operators, String type,
                                                               Boolean computeVolume) {
        try {
            BigQueryConnection connection = new BigQueryConnection();
            String query = "SELECT  " +
                    "  SUM( " +
                    "    CASE WHEN type.category = 'BONUS' " +
                    "    THEN " +
                    "      CASE WHEN type.direction = 'CREDIT' " +
                    "      then trx.montant  " +
                    "      ELSE - trx.montant  " +
                    "      END " +
                    "    ELSE 0 " +
                    "    END " +
                    "  ) as bonus, " +
                    "  SUM( " +
                    "    CASE WHEN type.category = 'MISES' " +
                    "    THEN " +
                    "      CASE WHEN type.direction = 'CREDIT' " +
                    "      then trx.montant  " +
                    "      ELSE - trx.montant  " +
                    "      END " +
                    "    ELSE 0 " +
                    "    END " +
                    "  ) as mises, " +
                    "  SUM( " +
                    "    CASE WHEN type.category = 'GAIN' " +
                    "    THEN " +
                    "      CASE WHEN type.direction = 'CREDIT' " +
                    "      then trx.montant  " +
                    "      ELSE - trx.montant  " +
                    "      END " +
                    "    ELSE 0 " +
                    "    END " +
                    "  ) as gain, " +
                    "  SUM( " +
                    "    CASE WHEN type.category = 'PAY_IN' " +
                    "    THEN " +
                    "      CASE WHEN type.direction = 'CREDIT' " +
                    "      then trx.montant  " +
                    "      ELSE - trx.montant  " +
                    "      END " +
                    "    ELSE 0 " +
                    "    END " +
                    "  ) as payin, " +
                    "  SUM( " +
                    "    CASE WHEN type.category = 'PAY_OUT' " +
                    "    THEN " +
                    "      CASE WHEN type.direction = 'CREDIT' " +
                    "      then trx.montant  " +
                    "      ELSE - trx.montant  " +
                    "      END " +
                    "    ELSE 0 " +
                    "    END " +
                    "  ) as payout, " +
                    "  COUNT( " +
                    "    CASE WHEN type.category = 'BONUS' " +
                    "    THEN 1 " +
                    "    END " +
                    "  ) as bonusCount, " +
                    "  COUNT( " +
                    "    CASE WHEN type.category = 'MISES' " +
                    "    THEN 1 " +
                    "    END " +
                    "  ) as misesCount, " +
                    "  COUNT( " +
                    "    CASE WHEN type.category = 'GAIN' " +
                    "    THEN 1 " +
                    "    END " +
                    "  ) as gainCount, " +
                    "  COUNT( " +
                    "    CASE WHEN type.category = 'PAY_IN' " +
                    "    THEN 1 " +
                    "    END " +
                    "  ) as payinCount, " +
                    "  COUNT( " +
                    "    CASE WHEN type.category = 'PAY_OUT' " +
                    "    THEN 1 " +
                    "    END " +
                    "  ) as payoutCount, " +
                    "  SUM( " +
                    "    CASE WHEN type.category = 'GAIN' AND type.direction = 'DEBIT' AND  ABS(trx.montant) >= 500000" +
                    "    THEN   ABS(trx.montant)*0.15  " +
                    "    ELSE 0 " +
                    "    END " +
                    "  ) as withholding " +
                    (operators != null ? ", trx.operateur_id as operateur_id " : "") +
                    "  FROM " + connection.getLonaciTableRef() + " trx LEFT JOIN " + connection.getLonaciTypeTableRef() + " type ON trx.type_transaction = type.code " +
                    "  WHERE trx.date BETWEEN @startDate AND @endDate ";

            if (type != null)
                query += " AND trx.type_transaction = @type";
            if (computeVolume != null)
                query += " AND type.use_to_compute = @use_to_compute";
            if (operators != null) {
                query += " AND trx.operateur_id IN UNNEST(@operators)\n";
            }

            if (operators != null && type != null) {
                query += " GROUP BY trx.operateur_id, trx.type ";
            } else if (operators != null) {
                query += " GROUP BY trx.operateur_id ";
            } else if (type != null) {
                query += " GROUP BY trx.type ";
            }
            QueryJobConfiguration.Builder queryConfig = QueryJobConfiguration.newBuilder(query)
                    .addNamedParameter("startDate", QueryParameterValue.dateTime(SIMPLE_DATE_FORMAT_WITH_HOUR.format(startDate)))
                    .addNamedParameter("endDate", QueryParameterValue.dateTime(SIMPLE_DATE_FORMAT_WITH_HOUR.format(endDate)));

            if (operators != null)
                queryConfig.addNamedParameter("operators", QueryParameterValue.array(operators.toArray(new String[0]), StandardSQLTypeName.STRING));
            if (type != null)
                queryConfig.addNamedParameter("type", QueryParameterValue.string(type));
            if (computeVolume != null)
                queryConfig.addNamedParameter("use_to_compute", QueryParameterValue.int64((Boolean.TRUE.equals(computeVolume) ? 1 : 0)));

            BigQuery bigquery = connection.getConnection();
            TableResult result = bigquery.query(queryConfig.build());

            Schema schema = result.getSchema();
            List<Map<String, String>> operatorLines = new ArrayList<>();

            for (FieldValueList row : result.iterateAll()) {
                Map<String, String> m = new HashMap<>();
                for (Field field : schema.getFields()) {
                    String value = this.getStringValue(field.getName(), row.get(field.getName()));
                    m.put(field.getName(), value);
                }
                operatorLines.add(m);
            }
            return operatorLines;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public Integer getActiveClients(Date startDate, Date endDate, String operator, String type, String country) {
        try {
            BigQueryConnection connection = new BigQueryConnection();
            String query = "SELECT COUNT(DISTINCT destinataire) as number FROM "+ connection.getLonaciTableRef() +" trx "
                    + " WHERE trx.date BETWEEN @startDate AND @endDate ";
            if (operator != null)
                query += " AND operateur_id = @operator";
            if (country != null)
                query += " AND country = @country";
            if (type != null)
                query += " AND type_transaction = @type";

            QueryJobConfiguration.Builder queryConfig = QueryJobConfiguration.newBuilder(query)
                    .addNamedParameter("startDate", QueryParameterValue.dateTime(SIMPLE_DATE_FORMAT_WITH_HOUR.format(startDate)))
                    .addNamedParameter("endDate", QueryParameterValue.dateTime(SIMPLE_DATE_FORMAT_WITH_HOUR.format(endDate)));

            if (operator != null)
                queryConfig.addNamedParameter("operator", QueryParameterValue.string(operator));
            if (country != null)
                queryConfig.addNamedParameter("country", QueryParameterValue.string(country));
            if (type != null)
                queryConfig.addNamedParameter("type", QueryParameterValue.string(type));

            BigQuery bigquery = connection.getConnection();
            TableResult result = bigquery.query(queryConfig.build());

            Schema schema = result.getSchema();
            for (FieldValueList row : result.iterateAll()) {
                for (Field field : schema.getFields()) {
                    String value = this.getStringValue(field.getName(), row.get(field.getName()));
                    return Integer.valueOf(value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public List<Map<String, String>> getSumClientsBetweenDates(Date startDate, Date endDate, String operator, String type) {
        try {
            BigQueryConnection connection = new BigQueryConnection();
            String query = "SELECT trx.destinataire as client, SUM(trx.montant) as sum, COUNT(*) as number FROM "+ connection.getLonaciTableRef() +" trx "
                    + " WHERE trx.date BETWEEN @startDate AND @endDate ";
            if (operator != null)
                query += " AND operateur_id = @operator";
            if (type != null)
                query += " AND type_transaction = @type";
            query += " GROUP BY client;";
            QueryJobConfiguration.Builder queryConfig = QueryJobConfiguration.newBuilder(query)
                    .addNamedParameter("startDate", QueryParameterValue.date(SIMPLE_DATE_FORMAT.format(startDate)))
                    .addNamedParameter("endDate", QueryParameterValue.date(SIMPLE_DATE_FORMAT.format(endDate)));

            if (operator != null)
                queryConfig.addNamedParameter("operator", QueryParameterValue.string(operator));
            if (type != null)
                queryConfig.addNamedParameter("type", QueryParameterValue.string(type));

            BigQuery bigquery = connection.getConnection();
            TableResult result = bigquery.query(queryConfig.build());

            List<Map<String, String>> responses = new ArrayList<>();
            Schema schema = result.getSchema();
            for (FieldValueList row : result.iterateAll()) {
                Map<String, String> m = new HashMap<>();
                for (Field field : schema.getFields()) {
                    String value = this.getStringValue(field.getName(), row.get(field.getName()));
                    if(field.getName().equals("sum"))
                        m.put(field.getName(), Utils.formatLabelAmount(Double.valueOf(value)));
                    else
                        m.put(field.getName(), value);
                }
                responses.add(m);
            }
            return responses;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    @Override
    public Map<String, String> getSumBetweenDatesWithCategoryAndUseToCompute(Date startDate, Date endDate, String operator, String category, Boolean useToCompute){
        try {
            BigQueryConnection connection = new BigQueryConnection();
            String query = "SELECT COUNT(*) as number, SUM( CASE WHEN type.direction = 'CREDIT' then trx.montant ELSE - trx.montant END ) as sum FROM "+ connection.getLonaciTableRef() +" trx LEFT JOIN " + connection.getLonaciTypeTableRef() + " type ON trx.type_transaction = type.code "
                    + " WHERE trx.date BETWEEN @startDate AND @endDate ";
            if (operator != null)
                query += " AND operateur_id = @operator";
            if (category != null)
                query += " AND type.category = @category";
            if (useToCompute != null)
                query += " AND type.use_to_compute = @useToCompute";

            QueryJobConfiguration.Builder queryConfig = QueryJobConfiguration.newBuilder(query)
                    .addNamedParameter("startDate", QueryParameterValue.dateTime(SIMPLE_DATE_FORMAT_WITH_HOUR.format(startDate)))
                    .addNamedParameter("endDate", QueryParameterValue.dateTime(SIMPLE_DATE_FORMAT_WITH_HOUR.format(endDate)));

            if (operator != null)
                queryConfig.addNamedParameter("operator", QueryParameterValue.string(operator));
            if (category != null)
                queryConfig.addNamedParameter("category", QueryParameterValue.string(category));
            if (useToCompute != null)
                queryConfig.addNamedParameter("useToCompute", QueryParameterValue.int64((Boolean.TRUE.equals(useToCompute) ? 1 : 0)));

            BigQuery bigquery = connection.getConnection();
            TableResult result = bigquery.query(queryConfig.build());

            Schema schema = result.getSchema();
            Map<String, String> m = new HashMap<>();
            for (FieldValueList row : result.iterateAll()) {
                for (Field field : schema.getFields()) {
                    String value = this.getStringValue(field.getName(), row.get(field.getName()));
                    m.put(field.getName(), value);
                }
                break;
            }
            return m;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    private String getStringValue(String name, FieldValue fieldValue) {
        try {
            return fieldValue.getStringValue();
        } catch (Exception e) {
            if("sum".equals(name) || "number".equals(name)) {
                return "0";
            }
        }
        return "";
    }

    private String getGrouper(AggregationTimeEnum time, String column) {
        if (time.equals(AggregationTimeEnum.DAY))
            return "EXTRACT(HOUR FROM " + column + ")";
        if (time.equals(AggregationTimeEnum.WEEK))
            return "DATE(" + column + ")";
        if (time.equals(AggregationTimeEnum.MONTH))
            return "DATE(" + column + ")";
        else return "";
    }

    private String getFormattedGrouper(AggregationTimeEnum time, String column) {
        if (time.equals(AggregationTimeEnum.DAY))
            return "FORMAT_DATETIME('%Y-%m-%d %H:00:00', CAST(" + column + " AS DATETIME))";
        if (time.equals(AggregationTimeEnum.WEEK))
            return "FORMAT_DATETIME('%Y-%m-%d', CAST(" + column + " AS DATETIME))";
        if (time.equals(AggregationTimeEnum.MONTH))
            return "FORMAT_DATETIME('%Y-%m-%d', CAST(" + column + " AS DATETIME))";
        if (time.equals(AggregationTimeEnum.YEAR))
            return "FORMAT_DATETIME('%Y-%m', CAST(" + column + " AS DATETIME))";
        else return "";
    }

}
