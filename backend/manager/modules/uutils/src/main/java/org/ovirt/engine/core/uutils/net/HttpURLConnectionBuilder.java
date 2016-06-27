package org.ovirt.engine.core.uutils.net;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;

public class HttpURLConnectionBuilder {

    private Boolean verifyHost = true;
    private Boolean verifyChain = true;
    private String httpsProtocol = "TLSv1";
    private String trustManagerAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
    private String trustStore;
    private String trustStoreType = KeyStore.getDefaultType();
    private String trustStorePassword = "changeit";
    private Integer connectTimeout;
    private Integer readTimeout;
    private URL url;

    public HttpURLConnectionBuilder() {
    }

    public HttpURLConnectionBuilder(URL url) {
        setURL(url);
    }

    public HttpURLConnectionBuilder(String url) {
        setURL(url);
    }

    public HttpURLConnectionBuilder setURL(URL url) {
        if (url != null && !url.getProtocol().equalsIgnoreCase("http") && !url.getProtocol().equalsIgnoreCase("https")) {
            throw new IllegalArgumentException(String.format("The URL %1$s  does not denote to an HTTP or HTTPS URL", url));
        }
        this.url = url;
        return this;
    }

    public HttpURLConnectionBuilder setURL(String url) {
        try {
            setURL(url != null ? new URL(url) : null);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(String.format("%1$s is not a valid URL", url));
        }
        return this;
    }

    public HttpURLConnectionBuilder setVerifyHost(Boolean verifyHost) {
        this.verifyHost = verifyHost;
        return this;
    }

    public HttpURLConnectionBuilder setVerifyChain(Boolean verifyChain) {
        this.verifyChain = verifyChain;
        return this;
    }

    public HttpURLConnectionBuilder setHttpsProtocol(String httpsProtocol) {
        this.httpsProtocol = httpsProtocol;
        return this;
    }

    public HttpURLConnectionBuilder setTrustManagerAlgorithm(String trustManagerAlgorithm) {
        this.trustManagerAlgorithm = trustManagerAlgorithm;
        return this;
    }

    public HttpURLConnectionBuilder setTrustStore(String trustStore) {
        this.trustStore = trustStore;
        return this;
    }

    public HttpURLConnectionBuilder setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
        return this;
    }

    public HttpURLConnectionBuilder setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
        return this;
    }

    public HttpURLConnectionBuilder setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public HttpURLConnectionBuilder setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public HttpURLConnectionBuilder appendRelativePath(URL url, String relativePath) throws MalformedURLException {
        this.url =
                new URL(url.getProtocol(),
                        url.getHost(),
                        url.getPort() == -1 ? url.getDefaultPort() : url.getPort(),
                        Paths.get(url.getPath(), relativePath).toString());
        return this;
    }

    public HttpURLConnection create() throws IOException, GeneralSecurityException {
        URLConnection connection = url.openConnection();
        connection.setAllowUserInteraction(false);
        connection.setUseCaches(false);
        if (connectTimeout != null) {
            connection.setConnectTimeout(connectTimeout);
        }
        if (readTimeout != null) {
            connection.setReadTimeout(readTimeout);
        }
        if (connection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
            TrustManager[] tm = null;
            if (verifyChain) {
                if (trustStore != null) {
                    try (InputStream is = new FileInputStream(trustStore)) {
                        KeyStore ks = KeyStore.getInstance(trustStoreType);
                        ks.load(is, StringUtils.isEmpty(trustStorePassword) ? null : trustStorePassword.toCharArray());
                        TrustManagerFactory tmf = TrustManagerFactory.getInstance(trustManagerAlgorithm);
                        tmf.init(ks);
                        tm = tmf.getTrustManagers();
                    }
                }
            } else {
                tm = new TrustManager[] {
                        new X509TrustManager() {
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[] {};
                            }

                            public void checkClientTrusted(
                                    java.security.cert.X509Certificate[] certs, String authType) {
                            }

                            public void checkServerTrusted(
                                    java.security.cert.X509Certificate[] certs, String authType) {
                            }
                        }
                };
            }


            SSLContext sslContext = SSLContext.getInstance(httpsProtocol);
            sslContext.init(null, tm, null);
            httpsConnection.setSSLSocketFactory(sslContext.getSocketFactory());

            if (!verifyHost) {
                httpsConnection.setHostnameVerifier(
                        (hostname, session) -> true
                );
            }
        }
        return (HttpURLConnection) connection;
    }

}
