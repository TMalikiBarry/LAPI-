package sn.intouch.gu.lonaciapi.ejb.bigquery.services;

import com.google.cloud.bigquery.*;
import sn.intouch.gu.lonaciapi.ejb.bigquery.BigQueryConnection;
import sn.intouch.gu.lonaciapi.ejb.bigquery.enums.AggregationTimeEnum;
import sn.intouch.gu.lonaciapi.ejb.utils.Utils;

import javax.ejb.Stateless;
import java.text.SimpleDateFormat;
import java.util.*;

@Stateless
public class BigQueryServiceBean implements BigQueryService{

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    @Override
    public List<Map<String, String>> getAggregation(Date startDate, Date endDate, AggregationTimeEnum time, String operator, String type) {
        BigQueryConnection connection = new BigQueryConnection();
        try {
            String query = "SELECT "+ this.getGrouper(time, "date") +" ddate, COUNT(*) as number, SUM(trx.montant) as sum FROM "+ connection.getLonaciTableRef() +" trx "
                    + " WHERE trx.date BETWEEN @startDate AND @endDate ";
            if (operator != null)
                query += " AND operateur_id = @operator";
            if (type != null)
                query += " AND type_transaction = @type";
            query += " GROUP BY ddate ORDER BY ddate ASC;";

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
                    if(field.getName().equals("sum"))
                        m.put(field.getName(), Utils.formatLabelAmount(Double.valueOf(row.get(field.getName()).getStringValue())));
                    else
                        m.put(field.getName(), row.get(field.getName()).getStringValue());
                }
                responses.add(m);
            }
            return responses;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
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

    public Map<String, String> getSumBetweenDates(Date startDate, Date endDate, String operator, String type) {
        try {
            BigQueryConnection connection = new BigQueryConnection();
            String query = "SELECT COUNT(*) as number, SUM(trx.montant) as sum FROM "+ connection.getLonaciTableRef() +" trx "
                    + " WHERE trx.date BETWEEN @startDate AND @endDate ";
            if (operator != null)
                query += " AND operateur_id = @operator";
            if (type != null)
                query += " AND type_transaction = @type";

            QueryJobConfiguration.Builder queryConfig = QueryJobConfiguration.newBuilder(query)
                    .addNamedParameter("startDate", QueryParameterValue.date(SIMPLE_DATE_FORMAT.format(startDate)))
                    .addNamedParameter("endDate", QueryParameterValue.date(SIMPLE_DATE_FORMAT.format(endDate)));

            if (operator != null)
                queryConfig.addNamedParameter("operator", QueryParameterValue.string(operator));
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
}
