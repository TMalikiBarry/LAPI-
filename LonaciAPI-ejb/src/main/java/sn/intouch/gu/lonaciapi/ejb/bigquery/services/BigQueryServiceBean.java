package sn.intouch.gu.lonaciapi.ejb.bigquery.services;

import com.google.cloud.bigquery.*;
import sn.intouch.gu.lonaciapi.ejb.bigquery.BigQueryConnection;
import sn.intouch.gu.lonaciapi.ejb.bigquery.enums.HeaderTimeEnum;
import sn.intouch.gu.lonaciapi.ejb.utils.DateUtil;
import sn.intouch.gu.lonaciapi.ejb.utils.Utils;

import javax.ejb.Stateless;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.*;

@Stateless
public class BigQueryServiceBean implements BigQueryService{

    private static final String TABLE_REF = "hubsoinfra.LONACI.lonaci_trx";

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public List<Map<String, String>> getAggregation(Date startDate, Date endDate, String operator, String type) {
        try {
            String query = "SELECT DATE(date) ddate, COUNT(*) as number, SUM(trx.montant) as sum FROM "+ TABLE_REF +" trx "
                    + " WHERE trx.date BETWEEN @startDate AND @endDate ";
            if (operator != null)
                query += " AND operateur_id = @operator";
            if (type != null)
                query += " AND type_transaction = @type";
            query += "  GROUP BY ddate ORDER BY ddate ASC;";

            QueryJobConfiguration.Builder queryConfig = QueryJobConfiguration.newBuilder(query)
                    .addNamedParameter("startDate", QueryParameterValue.date(SIMPLE_DATE_FORMAT.format(startDate)))
                    .addNamedParameter("endDate", QueryParameterValue.date(SIMPLE_DATE_FORMAT.format(endDate)));
            if (operator != null)
                queryConfig.addNamedParameter("operator", QueryParameterValue.string(operator));
            if (type != null)
                queryConfig.addNamedParameter("type", QueryParameterValue.string(type));

            BigQuery bigquery = new BigQueryConnection().getConnection();
            TableResult result = bigquery.query(queryConfig.build());

            List<Map<String, String>> responses = new ArrayList<>();
            Schema schema = result.getSchema();
            for (FieldValueList row : result.iterateAll()) {
                Map<String, String> m = new HashMap<>();
                for (Field field : schema.getFields()) {
                    if(field.getName().equals("sum"))
                        m.put(field.getName(), Utils.formatLabelAmount(Double.valueOf(row.get(field.getName()).getStringValue())));
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

    @Override
    public Map<String, String> getHeader(Date endDate, String operator, String type, HeaderTimeEnum timeEnum) {
        try {

            String query = "SELECT COUNT(*) as number, SUM(trx.montant) as sum FROM "+ TABLE_REF +" trx "
                    + " WHERE trx.date BETWEEN @startDate AND @endDate ";
            if (operator != null)
                query += " AND operateur_id = @operator";
            if (type != null)
                query += " AND type_transaction = @type";

            QueryJobConfiguration.Builder queryConfig = QueryJobConfiguration.newBuilder(query)
                    .addNamedParameter("startDate", QueryParameterValue.date(SIMPLE_DATE_FORMAT.format(getStartDateString(timeEnum))))
                    .addNamedParameter("endDate", QueryParameterValue.date(SIMPLE_DATE_FORMAT.format(endDate)));

            if (operator != null)
                queryConfig.addNamedParameter("operator", QueryParameterValue.string(operator));
            if (type != null)
                queryConfig.addNamedParameter("type", QueryParameterValue.string(type));

            BigQuery bigquery = new BigQueryConnection().getConnection();
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

    private Date getStartDateString(HeaderTimeEnum timeEnum) {;
        if (timeEnum.equals(HeaderTimeEnum.DAY))
            return Date.from(DateUtil.startOfDay().toInstant(ZoneOffset.UTC));
        else if(timeEnum.equals(HeaderTimeEnum.WEEK))
            return Date.from(DateUtil.startOfWeek().toInstant(ZoneOffset.UTC));
        else if (timeEnum.equals(HeaderTimeEnum.MONTH))
            return Date.from(DateUtil.startOfMonth().toInstant(ZoneOffset.UTC));
        return null;
    }
}
