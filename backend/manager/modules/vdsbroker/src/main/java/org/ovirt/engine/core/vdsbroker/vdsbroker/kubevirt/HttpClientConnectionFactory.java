package org.ovirt.engine.core.vdsbroker.vdsbroker.kubevirt;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

@Singleton
public class HttpClientConnectionFactory {
    private PoolingHttpClientConnectionManager connectionManager;

    public HttpClientConnectionFactory() {

    }

    @PostConstruct
    public void init() {
        connectionManager = new PoolingHttpClientConnectionManager();
    }

    @PreDestroy
    public void shutdown() {
        connectionManager.close();
    }

    @Produces @Singleton
    public CloseableHttpClient get() {
        return HttpClients.custom().setConnectionManager(connectionManager).build();
    }
}
