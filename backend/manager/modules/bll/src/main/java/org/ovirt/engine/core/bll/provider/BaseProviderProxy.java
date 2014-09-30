package org.ovirt.engine.core.bll.provider;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.util.List;

import javax.net.ssl.SSLException;

import org.apache.commons.codec.binary.Base64;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.uutils.crypto.CertificateChain;
import org.ovirt.engine.core.uutils.net.HttpURLConnectionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseProviderProxy implements ProviderProxy {

    public static final Logger log = LoggerFactory.getLogger(BaseProviderProxy.class);

    public static interface ResponseCodeHandler {
        void handle(final HttpURLConnection connection) throws IOException;
    }

    public static class ConnectionWrapper {

        protected HttpURLConnection connection;
        protected byte[] response;

        public ConnectionWrapper(final HttpURLConnection connection) {
            this.connection = connection;
        }

        public byte[] getResponse() throws IOException {
            if (response == null) {
                ByteArrayOutputStream bytesOs = new ByteArrayOutputStream();
                try (BufferedInputStream bis = new BufferedInputStream(connection.getInputStream())) {
                    beforeReadResponse();
                    byte[] buff = new byte[8196];
                    while (true) {
                        int read = bis.read(buff, 0, 8196);
                        if (read > 0) {
                            bytesOs.write(buff, 0, read);
                        } else {
                            break;
                        }
                    }
                    afterReadResponse();
                } catch (Exception ex) {
                    log.error("Exception is ", ex);
                }
                response = bytesOs.toByteArray();
            }
            return response;
        }

        protected void beforeReadResponse() throws Exception {
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK
                    && connection.getResponseCode() != HttpURLConnection.HTTP_MOVED_TEMP) {
                throw new VdcBLLException(VdcBllErrors.PROVIDER_FAILURE);
            }
        }

        protected void afterReadResponse() throws Exception {
        }

        public HttpURLConnection getConnection() {
            return connection;
        }

    }

    private URL url;
    private Provider<?> hostProvider;
    private byte[] response;

    protected static enum HttpMethodType {
        GET,
        POST,
        PUT,
        DELETE
    };

    public BaseProviderProxy(Provider<?> provider) {
        try {
            url = new URL(provider.getUrl());
            this.hostProvider = provider;
        } catch (MalformedURLException e) {
            handleException(e);
        }
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public List<? extends Certificate> getCertificateChain() {
        List<? extends Certificate> result = null;
        if (url.getProtocol().equalsIgnoreCase(String.valueOf("https"))) {
            try {
                result = CertificateChain.completeChain(
                        CertificateChain.getSSLPeerCertificates(url),
                        CertificateChain.keyStoreToTrustAnchors( ExternalTrustStoreInitializer.getTrustStore()));
            } catch (IOException | GeneralSecurityException e) {
                handleException(e);
            }
        }
        return result;
    }

    protected static void handleException(Exception e) {
        throw new VdcBLLException(VdcBllErrors.PROVIDER_FAILURE, e.getMessage());
    }

    protected ConnectionWrapper createConnection(String relativePath) {

        URL hostUrl = getUrl();
        HttpURLConnectionBuilder builder = null;
        HttpURLConnection result = null;
        try {
            builder = new HttpURLConnectionBuilder().appendRelativePath(hostUrl, relativePath);
            if (new File(EngineLocalConfig.getInstance().getExternalProvidersTrustStore().getAbsolutePath()).exists()) {
                builder
                    .setTrustStore(
                            EngineLocalConfig.getInstance().getExternalProvidersTrustStore().getAbsolutePath())
                    .setTrustStorePassword(EngineLocalConfig.getInstance().getExternalProvidersTrustStorePassword())
                    .setTrustStoreType(EngineLocalConfig.getInstance().getExternalProvidersTrustStoreType());
            }
            result = builder.create();
            handleCredentials(result);
        } catch (Exception ex) {
            log.error("Cannot communicate with provider", ex);
            throw new VdcBLLException(VdcBllErrors.PROVIDER_FAILURE);
        }

        return createWrapper(result);
    }

    protected ConnectionWrapper createWrapper(HttpURLConnection result) {
        return new ConnectionWrapper(result);
    }


    protected void handleCredentials(HttpURLConnection connection) {
        if (hostProvider.getUsername() != null && hostProvider.getPassword() != null) {
            connection.setRequestProperty(
                    "Authorization",
                    String.format("Basic %1$s",
                            new Base64(0).encodeToString(
                                    String.format("%1$s:%2$s", hostProvider.getUsername(), hostProvider.getPassword())
                                            .getBytes(Charset.forName("UTF-8")))
                            )
                            .toString());
        }
    }

    protected byte[] runHttpMethod(HttpMethodType httpMethod,
            String contentType,
            String relativeUrl,
            String body) {
        ConnectionWrapper wrapper = createConnection(relativeUrl);
        return runHttpMethod(httpMethod, contentType, relativeUrl, body, wrapper);
    }

    protected byte[] runHttpMethod(HttpMethodType httpMethod,
            String contentType,
            String relativeUrl,
            String body, ConnectionWrapper wrapper) {
        byte[] result = null;
        try {
            response = null;
            wrapper.getConnection().setRequestProperty("Content-Type", contentType);
            wrapper.getConnection().setDoInput(true);
            wrapper.getConnection().setDoOutput(httpMethod != HttpMethodType.GET);
            wrapper.getConnection().setRequestMethod(httpMethod.toString());
            if (body != null) {
                byte[] bytes = body.getBytes(Charset.forName("UTF-8"));
                wrapper.getConnection().setRequestProperty("Content-Length",
                        new StringBuilder().append(bytes.length).toString());

                try (OutputStream outputStream = wrapper.getConnection().getOutputStream()) {
                    outputStream.write(bytes);
                }
            }
            result = wrapper.getResponse();
        } catch (SSLException e) {
            throw new VdcBLLException(VdcBllErrors.PROVIDER_SSL_FAILURE, e.getMessage());
        } catch (IOException e) {
            handleException(e);
        } finally {
            if (wrapper != null) {
                wrapper.getConnection().disconnect();
            }
        }
        return result;
    }


}
