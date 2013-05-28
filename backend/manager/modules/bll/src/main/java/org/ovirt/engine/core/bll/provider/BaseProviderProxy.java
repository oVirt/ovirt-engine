package org.ovirt.engine.core.bll.provider;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.HttpURL;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;

public abstract class BaseProviderProxy implements ProviderProxy {

    private URL url;
    private static final String SSL_PROTOCOL = "SSLv3";

    public BaseProviderProxy(Provider provider) {
        try {
            url = new URL(provider.getUrl());
        } catch (MalformedURLException e) {
            handleException(e);
        }
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public List<? extends Certificate> getCertificateChain() {
        if (!isSecured()) {
            return null;
        }

        HttpsURLConnection conn = null;
        final List<X509Certificate> chain = new ArrayList<X509Certificate>();
        try {
            SSLContext ctx;
            ctx = SSLContext.getInstance(SSL_PROTOCOL);
            ctx.init(null, createTrustManager(chain), null);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(ctx.getSocketFactory());
            conn.connect();
            return chain;
        } catch (SSLHandshakeException e) {
            return chain;
        } catch (NoSuchAlgorithmException e) {
            handleException(e);
        } catch (KeyManagementException e) {
            handleException(e);
        } catch (IOException e) {
            handleException(e);
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    protected static void handleException(Exception e) {
        throw new VdcBLLException(VdcBllErrors.PROVIDER_FAILURE, e.getMessage());
    }

    protected boolean isSecured() {
        boolean secured = true;
        if (url.getProtocol().equalsIgnoreCase(String.valueOf(HttpURL.DEFAULT_SCHEME))) {
            secured = false;
        }
        return secured;
    }

    private TrustManager[] createTrustManager(final List<X509Certificate> chain) {
        return new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[] {};
                    }
                    public void checkClientTrusted(
                        X509Certificate[] certs,
                        String authType
                    ) {
                    }
                    public void checkServerTrusted(
                        X509Certificate[] certs,
                        String authType
                    ) {
                        // Still need to verify that the root certificate is returned also
                        // when it is trusted by the JRE truststore (cacerts file)
                        for (X509Certificate cert : certs) {
                            chain.add(cert);
                        }
                    }
                }
        };
    }
}
