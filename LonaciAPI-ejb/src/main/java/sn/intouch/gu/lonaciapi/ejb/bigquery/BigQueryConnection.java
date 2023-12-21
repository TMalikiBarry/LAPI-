package sn.intouch.gu.lonaciapi.ejb.bigquery;


import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;

import javax.ejb.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Singleton
public class BigQueryConnection {

    public BigQueryConnection () {
    }
    private BigQuery bigQuery = null;

    public BigQuery getConnection() {
        if (bigQuery == null) {
            ServiceAccountCredentials credentials;
            File credentialsPath = new File("C:/Users/AliouneSARR/Documents/BIGQUERY/hubsoinfra-2be8d194727b.json");

            try (FileInputStream serviceAccountStream = new FileInputStream(credentialsPath)) {
                credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            bigQuery = BigQueryOptions.newBuilder()
                    .setCredentials(credentials)
                    .setProjectId(credentials.getProjectId())
                    .build().getService();
        }
        return bigQuery;
    }
}
