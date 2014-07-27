package org.ovirt.engine.core.uutils.servlet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ProxyServletBase extends HttpServlet {

    private static final long serialVersionUID = 5331291232426186121L;

    private Boolean verifyHost = true;
    private Boolean verifyChain = true;
    private String httpsProtocol;
    private String trustManagerAlgorithm;
    private String trustStore;
    private String trustStoreType;
    private String trustStorePassword = "changeit";
    private Integer readTimeout;
    private String url;

    protected static long copy(final InputStream input, final OutputStream output) throws IOException {
        final byte[] buffer = new byte[8*1024];
        long count = 0;
        int n;
        while ((n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    protected void setVerifyHost(Boolean verifyHost) {
        this.verifyHost = verifyHost;
    }

    protected void setVerifyChain(Boolean verifyChain) {
        this.verifyChain = verifyChain;
    }

    protected void setHttpsProtocol(String httpsProtocol) {
        this.httpsProtocol = httpsProtocol;
    }

    protected void setTrustManagerAlgorithm(String trustManagerAlgorithm) {
        this.trustManagerAlgorithm = trustManagerAlgorithm;
    }

    protected void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    protected void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    protected void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    protected void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    protected void setUrl(String url) {
        this.url = url;
    }

    protected URLConnection createConnection(URL url) throws IOException, GeneralSecurityException {
        URLConnection connection = url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.setAllowUserInteraction(false);
        connection.setUseCaches(false);
        if (readTimeout != null) {
            connection.setReadTimeout(readTimeout);
        }
        if (connection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection)connection;
            TrustManager[] tm = null;
            if (verifyChain) {
                if (trustStore != null) {
                    try(InputStream is = new FileInputStream(trustStore)) {
                        KeyStore ks = KeyStore.getInstance(trustStoreType);
                        ks.load(is, trustStorePassword.toCharArray());
                        TrustManagerFactory tmf = TrustManagerFactory.getInstance(trustManagerAlgorithm);
                        tmf.init(ks);
                        tm = tmf.getTrustManagers();
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
            }

            SSLContext sslContext = SSLContext.getInstance(httpsProtocol);
            sslContext.init(null, tm, null);
            httpsConnection.setSSLSocketFactory(sslContext.getSocketFactory());

            if (!verifyHost) {
                httpsConnection.setHostnameVerifier(
                    new HostnameVerifier() {
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    }
                );
            }
        }

        return connection;
    }

    private String mergeQuery(String url, String queryString) throws MalformedURLException {
        String ret = url;
        if (queryString != null) {
            URL u = new URL(ret);
            if (u.getQuery() == null) {
                ret += "?";
            } else {
                ret += "&";
            }
            ret += queryString;
        }
        return ret;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            if (verifyHost == null) {
                verifyHost = true;
            }
            if (verifyChain == null) {
                verifyChain = true;
            }
            if (trustManagerAlgorithm == null) {
                trustManagerAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            }
            if (trustStoreType == null) {
                trustStoreType = KeyStore.getDefaultType();
            }
            if (httpsProtocol == null) {
                httpsProtocol = "TLSv1";
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        if (url == null) {
            response.sendError(response.SC_NOT_FOUND, "Cannot proxy, no URL is configured.");
        } else {
            URLConnection connection;
            try {
                connection = createConnection(new URL(mergeQuery(url, request.getQueryString())));
            } catch(Exception e) {
                throw new ServletException(e);
            }
            connection.connect();
            try {
                if (connection instanceof HttpURLConnection) {
                    response.setStatus(((HttpURLConnection)connection).getResponseCode());
                }
                for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
                    if (entry.getKey() != null) {
                        boolean first = true;
                        for (String value : entry.getValue()) {
                            if (first) {
                                first = false;
                                response.setHeader(entry.getKey(), value);
                            } else {
                                response.addHeader(entry.getKey(), value);
                            }
                        }
                    }
                }
                copy(connection.getInputStream(), response.getOutputStream());
            } finally {
                if (connection instanceof HttpURLConnection) {
                    ((HttpURLConnection)connection).disconnect();
                }
            }
        }
    }

}
