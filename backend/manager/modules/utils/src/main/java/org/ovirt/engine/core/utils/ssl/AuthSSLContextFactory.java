package org.ovirt.engine.core.utils.ssl;

import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.function.Supplier;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.ovirt.engine.core.utils.Result;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * AuthSSLContextFactory can be used to validate the identity of the HTTPS server against a list of trusted certificates
 * and to authenticate to the HTTPS server using a private key.
 * </p>
 *
 * <p>
 * AuthSSLContextFactory will enable server authentication when supplied with a {@link java.security.KeyStore
 * truststore} file containg one or several trusted certificates. The client secure socket will reject the connection
 * during the SSL session handshake if the target HTTPS server attempts to authenticate itself with a non-trusted
 * certificate.
 * </p>
 *
 * <p>
 * Use JDK keytool utility to import a trusted certificate and generate a truststore file:
 *
 * <pre>
 *     keytool -import -alias "my server cert" -file server.crt -keystore my.truststore
 * </pre>
 *
 * </p>
 *
 * <p>
 * AuthSSLContextFactory will enable client authentication when supplied with a {@link java.security.KeyStore keystore}
 * file containg a private key/public certificate pair. The client secure socket will use the private key to
 * authenticate itself to the target HTTPS server during the SSL session handshake if requested to do so by the server.
 * The target HTTPS server will in its turn verify the certificate presented by the client in order to establish
 * client's authenticity
 * </p>
 *
 * <p>
 * Use the following sequence of actions to generate a keystore file
 * </p>
 * <ul>
 * <li>
 * <p>
 * Use JDK keytool utility to generate a new key
 *
 * <pre>
 * keytool -genkey -v -alias "my client key" -validity 365 -keystore my.keystore
 * </pre>
 *
 * For simplicity use the same password for the key as that of the keystore
 * </p>
 * </li>
 * <li>
 * <p>
 * Issue a certificate signing request (CSR)
 *
 * <pre>
 * keytool -certreq -alias "my client key" -file mycertreq.csr -keystore my.keystore
 * </pre>
 *
 * </p>
 * </li>
 * <li>
 * <p>
 * Send the certificate request to the trusted Certificate Authority for signature. One may choose to act as her own CA
 * and sign the certificate request using a PKI tool, such as OpenSSL.
 * </p>
 * </li>
 * <li>
 * <p>
 * Import the trusted CA root certificate
 *
 * <pre>
 * keytool -import -alias "my trusted ca" -file caroot.crt -keystore my.keystore
 * </pre>
 *
 * </p>
 * </li>
 * <li>
 * <p>
 * Import the PKCS#7 file containg the complete certificate chain
 *
 * <pre>
 * keytool -import -alias "my client key" -file mycert.p7 -keystore my.keystore
 * </pre>
 *
 * </p>
 * </li>
 * <li>
 * <p>
 * Verify the content the resultant keystore file
 *
 * <pre>
 * keytool -list -v -keystore my.keystore
 * </pre>
 *
 * </p>
 * </li>
 * </ul>
 * <p>
 */
public class AuthSSLContextFactory {
    private static final Logger log = LoggerFactory.getLogger(AuthSSLContextFactory.class);
    private final Supplier<String> protocolSupplier;
    private EngineEncryptionManagersSupplier<KeyManager[]> keyManagersSupplier = EngineEncryptionUtils::getKeyManagers;
    private EngineEncryptionManagersSupplier<TrustManager[]> trustManagersSupplier =
            EngineEncryptionUtils::getTrustManagers;

    public AuthSSLContextFactory(Supplier<String> protocolSupplier) {
        this.protocolSupplier = protocolSupplier;
    }

    public Result<String, SSLContext> createSSLContext() {
        try {
            TrustManager[] trustManagers = createTrustManagers();
            SSLContext sslcontext = SSLContext.getInstance(protocolSupplier.get());
            sslcontext.init(keyManagersSupplier.getManagers(), trustManagers, null);
            return Result.value(sslcontext);
        } catch (NoSuchAlgorithmException e) {
            return Result.error(handleError("Unsupported algorithm exception", e));
        } catch (KeyStoreException e) {
            return Result.error(handleError("Keystore exception", e));
        } catch (GeneralSecurityException e) {
            return Result.error(handleError("Key management exception", e));
        }
    }

    private String handleError(String errMsgBase, Exception e) {
        log.debug("Exception", e);
        return errMsgBase + ": " + e.getMessage();
    }

    private TrustManager[] createTrustManagers() throws GeneralSecurityException {
        log.debug("Initializing trust manager");
        TrustManager[] trustmanagers = trustManagersSupplier.getManagers();
        for (int i = 0; i < trustmanagers.length; i++) {
            if (trustmanagers[i] instanceof X509TrustManager) {
                trustmanagers[i] = new AuthSSLX509TrustManager((X509TrustManager) trustmanagers[i]);
            }
        }
        return trustmanagers;
    }

    public void setKeyManagersSupplier(EngineEncryptionManagersSupplier<KeyManager[]> keyManagersSupplier) {
        this.keyManagersSupplier = keyManagersSupplier;
    }

    public void setTrustManagersSupplier(EngineEncryptionManagersSupplier<TrustManager[]> trustManagersSupplier) {
        this.trustManagersSupplier = trustManagersSupplier;
    }

    @FunctionalInterface
    public interface EngineEncryptionManagersSupplier<T> {
        T getManagers() throws GeneralSecurityException;
    }
}
