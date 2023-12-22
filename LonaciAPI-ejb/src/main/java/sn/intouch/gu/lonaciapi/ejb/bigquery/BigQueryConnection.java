package sn.intouch.gu.lonaciapi.ejb.bigquery;


import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.parameter.entities.Parameter;
import sn.intouch.gu.lonaciapi.ejb.parameter.services.ParameterService;

import javax.ejb.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Singleton
public class BigQueryConnection {

    public BigQueryConnection () {
    }

    private static final String BIGQUERY_CONF_FILE_PATH = "BIGQUERY_CONF_FILE_PATH";
    private static final String LONACI_TABLE_REF = "LONACI_TABLE_REF";

    private BigQuery bigQuery = null;
    private String lonaciTableRef = null;

    public BigQuery getConnection() {
        if (bigQuery == null) {
            ParameterService parameterService = (ParameterService) JNDIUtils.lookUpEJB(EJBRegistry.ParameterServiceBean);
            Parameter pathParam = parameterService.getParameterByCode(BIGQUERY_CONF_FILE_PATH);
            if (pathParam == null)
                throw new RuntimeException("Cannot find parameter of code :: " + BIGQUERY_CONF_FILE_PATH);

            ServiceAccountCredentials credentials;
            File credentialsPath = new File(pathParam.getPrmStringValue());

            try (FileInputStream serviceAccountStream = new FileInputStream(credentialsPath)) {
                credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            bigQuery = BigQueryOptions.newBuilder()
                    .setCredentials(credentials)
                    .setProjectId(credentials.getProjectId())
                    .build().getService();

            Parameter tableParam = parameterService.getParameterByCode(LONACI_TABLE_REF);
            if (tableParam == null)
                throw new RuntimeException("Cannot find parameter of code :: " + LONACI_TABLE_REF);
            lonaciTableRef = tableParam.getPrmStringValue();
        }
        return bigQuery;
    }

    public String getLonaciTableRef() {
        this.getConnection();
        return lonaciTableRef;
    }
}
