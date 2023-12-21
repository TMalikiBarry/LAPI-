package sn.intouch.gu.lonaciapi.ejb.bigquery.services;

import com.google.cloud.bigquery.*;
import sn.intouch.gu.lonaciapi.ejb.bigquery.BigQueryConnection;

import javax.ejb.Stateless;
import java.text.SimpleDateFormat;
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
                query += " operateur_id = @operator";
            if (type != null)
                query += " type_transaction = @type";
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

    /*public static void main(String[] args) {
        BigQueryServiceBean bean = new BigQueryServiceBean();
        List<Map<String, String>> maps = bean.getAggregation(new Date(new Date().getTime() - (1000 * 3600 * 24 * 10)), new Date(), null, null);
        System.out.println(new Gson().toJson(maps));
    }*/
}
