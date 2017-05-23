package org.ovirt.engine.core.bll.provider.network.openstack;

import static java.lang.Math.toIntExact;

import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.ovirt.engine.core.bll.provider.ExternalTrustStoreInitializer;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woorea.openstack.connector.RESTEasyConnector;

public class CustomizedRESTEasyConnector extends RESTEasyConnector {

    private static Logger log = LoggerFactory.getLogger(CustomizedRESTEasyConnector.class);

    @Override
    protected ClientExecutor createClientExecutor() {

        DefaultHttpClient httpClient = new DefaultHttpClient();

        configureTimeouts(httpClient);

        registerExternalProvidersTrustStore(httpClient);

        return new ApacheHttpClient4Executor(httpClient);
    }

    private void configureTimeouts(DefaultHttpClient httpClient) {
        long socketTimeOut = TimeUnit.SECONDS.toMillis(
                Config.<Integer> getValue(ConfigValues.ExternalNetworkProviderTimeout));

        long connectionTimeOut = TimeUnit.SECONDS.toMillis(
                Config.<Integer> getValue(ConfigValues.ExternalNetworkProviderConnectionTimeout));

        HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, toIntExact(connectionTimeOut));
        HttpConnectionParams.setSoTimeout(params, toIntExact(socketTimeOut));
    }

    private void registerExternalProvidersTrustStore(DefaultHttpClient httpClient) {
        try {
            KeyStore trustStore = ExternalTrustStoreInitializer.getTrustStore();
            SSLSocketFactory socketFactory = new SSLSocketFactory(trustStore);
            Scheme scheme = new Scheme("https", 443, socketFactory);
            httpClient.getConnectionManager().getSchemeRegistry().register(scheme);
        } catch (Exception ex) {
            log.warn("Cannot register external providers trust store: {}", ex.getMessage());
            log.debug("Exception", ex);
        }
    }
}
