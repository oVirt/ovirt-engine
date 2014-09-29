package org.ovirt.engine.core.uutils.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.List;
import java.util.Map;

import javax.net.ssl.TrustManagerFactory;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.uutils.net.HttpURLConnectionBuilder;

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

    protected HttpURLConnection create(URL url) throws IOException, GeneralSecurityException {
        return new HttpURLConnectionBuilder(url).setHttpsProtocol(httpsProtocol)
                .setReadTimeout(readTimeout)
                .setTrustManagerAlgorithm(trustManagerAlgorithm)
                .setTrustStore(trustStore)
                .setTrustStorePassword(trustStorePassword)
                .setTrustStoreType(trustStoreType)
                .setURL(url)
                .setVerifyChain(verifyChain)
                .setVerifyHost(verifyHost).create();
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
            HttpURLConnection connection = null;
            try {
                connection = create(new URL(mergeQuery(url, request.getQueryString())));
                connection.setDoInput(true);
                connection.setDoOutput(false);
                response.setStatus(connection.getResponseCode());
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
                connection.connect();
            } catch (Exception e) {
                throw new ServletException(e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

}
