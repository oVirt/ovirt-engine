package org.ovirt.engine.core.utils.ssl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.util.Optional;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.utils.Result;

class AuthSSLContextFactoryTest {

    private static final String PROTOCOL = "TLS";
    private AuthSSLContextFactory factory;

    @BeforeEach
    public void setup() {
        factory = new AuthSSLContextFactory(() -> PROTOCOL);
        factory.setKeyManagersSupplier(() -> new KeyManager[0]);
        factory.setTrustManagersSupplier(() -> new TrustManager[0]);
    }

    @Test
    public void shouldCreateSSLContext() {
        factory.setTrustManagersSupplier(() -> new TrustManager[] { mock(X509TrustManager.class) });

        Optional<SSLContext> maybeSSLContext = factory.createSSLContext()
                .orError(e -> fail("No error expected but found: " + e));

        assertThat(maybeSSLContext.isPresent()).isTrue();
        assertThat(maybeSSLContext.get().getProtocol()).isEqualTo(PROTOCOL);
    }

    @Test
    public void shouldHandleNoSuchAlgorithmException() {
        factory = new AuthSSLContextFactory(() -> "UnknownRandomProtocol");
        factory.setKeyManagersSupplier(() -> new KeyManager[0]);
        factory.setTrustManagersSupplier(() -> new TrustManager[] { mock(X509TrustManager.class) });

        Result<String, SSLContext> result = factory.createSSLContext();

        Optional<SSLContext> maybeSSLContext = result.orError(e -> assertThat(e)
                .isEqualTo("Unsupported algorithm exception: UnknownRandomProtocol SSLContext not available"));
        assertThat(maybeSSLContext).isEqualTo(Optional.empty());
    }

    @Test
    public void shouldHandleKeyStoreException() {
        factory.setTrustManagersSupplier(() -> new TrustManager[] { mock(X509TrustManager.class) });
        factory.setKeyManagersSupplier(() -> {
            throw new KeyStoreException("test");
        });

        Result<String, SSLContext> result = factory.createSSLContext();

        Optional<SSLContext> maybeSSLContext = result.orError(e -> assertThat(e)
                .isEqualTo("Keystore exception: test"));
        assertThat(maybeSSLContext).isEqualTo(Optional.empty());
    }

    @Test
    public void shouldHandleGenericSecurityException() {
        factory.setTrustManagersSupplier(() -> new TrustManager[] { mock(X509TrustManager.class) });
        factory.setKeyManagersSupplier(() -> {
            throw new GeneralSecurityException("test");
        });

        Result<String, SSLContext> result = factory.createSSLContext();

        Optional<SSLContext> maybeSSLContext = result.orError(e -> assertThat(e)
                .isEqualTo("Key management exception: test"));
        assertThat(maybeSSLContext).isEqualTo(Optional.empty());
    }

}
