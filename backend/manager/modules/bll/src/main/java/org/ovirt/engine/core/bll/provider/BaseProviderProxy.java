package org.ovirt.engine.core.bll.provider;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.util.List;

import javax.net.ssl.SSLException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.uutils.crypto.CertificateChain;
import org.ovirt.engine.core.uutils.net.HttpURLConnectionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseProviderProxy implements ProviderProxy {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private URL url;
    private Provider<?> hostProvider;
    private ProviderValidator providerValidator;

    protected enum HttpMethodType {
        GET,
        POST,
        PUT,
        DELETE
    }

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
                log.error("Failed to communicate with external provider '{}' due to error '{}'",
                        hostProvider.getName(),
                        ExceptionUtils.getRootCauseMessage(e));
                log.debug("Exception", e);
                handleException(e);
            }
        }
        return result;
    }

    protected static void handleException(Exception e) {
        throw new EngineException(EngineError.PROVIDER_FAILURE, e.getMessage());
    }

    protected void afterReadResponse(HttpURLConnection connection, byte[] response) throws Exception {
    }

    protected void beforeReadResponse(HttpURLConnection connection) throws Exception {
        if (isUnsuccessfulResponseCode(connection)) {
            throw new EngineException(EngineError.PROVIDER_FAILURE);
        }
    }

    protected boolean isUnsuccessfulResponseCode(HttpURLConnection connection) throws IOException {
        return connection.getResponseCode() < 200 || connection.getResponseCode() >= 400;
    }

    protected HttpURLConnection createConnection(String relativePath) {

        URL hostUrl = getUrl();
        HttpURLConnectionBuilder builder;
        HttpURLConnection result;
        try {
            builder = new HttpURLConnectionBuilder().appendRelativePath(hostUrl, relativePath);
            if (new File(EngineLocalConfig.getInstance().getExternalProvidersTrustStore().getAbsolutePath()).exists()) {
                builder
                    .setTrustStore(
                            EngineLocalConfig.getInstance().getExternalProvidersTrustStore().getAbsolutePath())
                    .setTrustStorePassword(EngineLocalConfig.getInstance().getExternalProvidersTrustStorePassword())
                    .setTrustStoreType(EngineLocalConfig.getInstance().getExternalProvidersTrustStoreType())
                    .setHttpsProtocol(Config.getValue(ConfigValues.ExternalCommunicationProtocol));
            }
            result = builder.create();
            handleCredentials(result);
        } catch (Exception ex) {
            log.error("Cannot communicate with provider", ex);
            throw new EngineException(EngineError.PROVIDER_FAILURE);
        }

        return result;

    }

    public byte[] getResponse(HttpURLConnection connection) throws IOException {
        byte[] response;
        ByteArrayOutputStream bytesOs = new ByteArrayOutputStream();
        try (BufferedInputStream bis = new BufferedInputStream(connection.getInputStream())) {
            beforeReadResponse(connection);
            byte[] buff = new byte[8196];
            while (true) {
                int read = bis.read(buff, 0, 8196);
                if (read > 0) {
                    bytesOs.write(buff, 0, read);
                } else {
                    break;
                }
            }
            response = bytesOs.toByteArray();
            afterReadResponse(connection, response);
        } catch (Exception ex) {
            log.error("Exception is {} ", ex.getMessage());
            log.debug("Exception: ", ex);
            if (ex instanceof EngineException) {
                throw (EngineException) ex;
            } else {
                throw new EngineException(EngineError.PROVIDER_FAILURE, ex.getMessage());
            }
        }
        return response;
    }


    protected void handleCredentials(HttpURLConnection connection) {
        if (hostProvider.getUsername() != null && hostProvider.getPassword() != null) {
            connection.setRequestProperty(
                    "Authorization",
                    String.format("Basic %1$s",
                            new Base64(0).encodeToString(
                                    String.format("%1$s:%2$s", hostProvider.getUsername(), hostProvider.getPassword())
                                            .getBytes(StandardCharsets.UTF_8))
                            )
            );
        }
    }

    protected byte[] runHttpMethod(HttpMethodType httpMethod,
            String contentType,
            String body, HttpURLConnection connection) {
        byte[] result = null;
        try {
            connection.setRequestProperty("Content-Type", contentType);
            connection.setDoInput(true);
            connection.setDoOutput(httpMethod != HttpMethodType.GET);
            connection.setRequestMethod(httpMethod.toString());
            if (body != null) {
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                connection.setRequestProperty("Content-Length", String.valueOf(bytes.length));

                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(bytes);
                }
            }
            result = getResponse(connection);
        } catch (SSLException e) {
            throw new EngineException(EngineError.PROVIDER_SSL_FAILURE, e.getMessage());
        } catch (IOException e) {
            handleException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    @Override
    public ProviderValidator getProviderValidator() {
        if (providerValidator == null) {
            providerValidator = new ProviderValidator(hostProvider);
        }
        return providerValidator;
    }

    public Provider<?> getProvider() {
        return hostProvider;
    }
}
