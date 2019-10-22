/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.core.uutils.net;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class HttpClientBuilder {
    private Boolean verifyHost = true;
    private Boolean verifyChain = true;
    private String tlsProtocol = "TLS";
    private String trustManagerAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
    private String trustStore;
    private String trustStoreType = KeyStore.getDefaultType();
    private String trustStorePassword = "changeit";
    private Integer connectTimeout;
    private Integer readTimeout;
    private Integer poolSize;
    private Integer retryCount;
    private Integer validateAfterInactivity;

    public HttpClientBuilder() {
    }

    public HttpClientBuilder setVerifyHost(Boolean verifyHost) {
        this.verifyHost = verifyHost;
        return this;
    }

    public HttpClientBuilder setVerifyChain(Boolean verifyChain) {
        this.verifyChain = verifyChain;
        return this;
    }

    public HttpClientBuilder setSslProtocol(String sslProtocol) {
        this.tlsProtocol = sslProtocol;
        return this;
    }

    public HttpClientBuilder setTrustManagerAlgorithm(String trustManagerAlgorithm) {
        this.trustManagerAlgorithm = trustManagerAlgorithm;
        return this;
    }

    public HttpClientBuilder setTrustStore(String trustStore) {
        this.trustStore = trustStore;
        return this;
    }

    public HttpClientBuilder setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
        return this;
    }

    public HttpClientBuilder setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
        return this;
    }

    public HttpClientBuilder setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public HttpClientBuilder setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public HttpClientBuilder setPoolSize(Integer poolSize) {
        this.poolSize = poolSize;
        return this;
    }

    public HttpClientBuilder setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    public HttpClientBuilder setValidateAfterInactivity(Integer validateAfterInactivity) {
        this.validateAfterInactivity = validateAfterInactivity;
        return this;
    }

    public CloseableHttpClient build() throws IOException, GeneralSecurityException {
        // Prepare the default configuration for all requests:
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeout != null? connectTimeout: 0)
                .setSocketTimeout(readTimeout != null? readTimeout: 0)
                .build();

        // Configure the trust manager:
        TrustManager[] trustManager = null;
        if (verifyChain) {
            if (trustStore != null) {
                try (InputStream is = new FileInputStream(trustStore)) {
                    KeyStore ks = KeyStore.getInstance(trustStoreType);
                    ks.load(is, StringUtils.isEmpty(trustStorePassword) ? null : trustStorePassword.toCharArray());
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(trustManagerAlgorithm);
                    tmf.init(ks);
                    trustManager = tmf.getTrustManagers();
                }
            }
        } else {
            trustManager = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[] {};
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
        }

        // Create the SSL context:
        SSLContext sslContext = SSLContext.getInstance(tlsProtocol);
        sslContext.init(null, trustManager, null);

        // Create the SSL host name verifier:
        HostnameVerifier sslHostnameVerifier = null;
        if (!verifyHost) {
            sslHostnameVerifier = (hostname, session) -> true;
        }

        // Create the socket factory for HTTP:
        ConnectionSocketFactory httpSocketFactory = new PlainConnectionSocketFactory();

        // Create the socket factory for HTTPS:
        ConnectionSocketFactory httpsSocketFactory = new SSLConnectionSocketFactory(sslContext, sslHostnameVerifier);

        // Create the socket factory registry:
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", httpSocketFactory)
                .register("https", httpsSocketFactory)
                .build();

        // Create the connection manager:
        HttpClientConnectionManager connectionManager;
        if (poolSize != null) {
            PoolingHttpClientConnectionManager poolManager =
                    new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            poolManager.setDefaultMaxPerRoute(poolSize);
            poolManager.setMaxTotal(poolSize);
            poolManager.setValidateAfterInactivity(validateAfterInactivity == null ? 100 : validateAfterInactivity);
            connectionManager = poolManager;
        } else {
            connectionManager = new BasicHttpClientConnectionManager(socketFactoryRegistry);
        }

        // Create the client:
        return org.apache.http.impl.client.HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setSSLHostnameVerifier(sslHostnameVerifier)
                .setConnectionManager(connectionManager)
                .setRetryHandler(new StandardHttpRequestRetryHandler(retryCount == null ? 1 : retryCount, true))
                .build();
    }
}
