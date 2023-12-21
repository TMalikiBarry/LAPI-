package sn.intouch.gu.lonaciapi.ejb.bigquery;


import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Test {
    public static void main(String[] args) {
        // TODO(developer): Replace this query before running the sample.
        String query = "SELECT corpus FROM `bigquery-public-data.samples.shakespeare` GROUP BY corpus;";
        simpleQuery(query);
    }

    public static void simpleQuery(String query) {
        try {
            // BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
            BigQuery bigquery = new BigQueryConnection().getConnection();

            // Create the query job.
            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();

            // Execute the query.
            TableResult result = bigquery.query(queryConfig);

            // Print the results.
            result.iterateAll().forEach(rows -> rows.forEach(field -> System.out.println(field.getValue())));

            System.out.println("Query ran successfully");
        } catch (BigQueryException | InterruptedException e) {
            System.out.println("Query did not run \n" + e.toString());
        }
    }
}
