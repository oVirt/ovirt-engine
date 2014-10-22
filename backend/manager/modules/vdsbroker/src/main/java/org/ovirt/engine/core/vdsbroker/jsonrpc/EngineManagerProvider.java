package org.ovirt.engine.core.vdsbroker.jsonrpc;

import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.vdsm.jsonrpc.client.reactors.ManagerProvider;

/**
 * Engine specific implementation of <code>ManagerProvider</code>
 * which provides <code>KeyManager</code>s and <code>TrustManager</code>.
 *
 */
public class EngineManagerProvider extends ManagerProvider {

    private String sslProtocol;

    public EngineManagerProvider(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }

    @Override
    public KeyManager[] getKeyManagers() throws GeneralSecurityException {
        return EngineEncryptionUtils.getKeyManagers();
    }

    @Override
    public TrustManager[] getTrustManagers() throws GeneralSecurityException {
        return EngineEncryptionUtils.getTrustManagers();
    }

    @Override
    public SSLContext getSSLContext() throws GeneralSecurityException {
        final SSLContext context;
        try {
            context = SSLContext.getInstance(this.sslProtocol);
            context.init(getKeyManagers(), getTrustManagers(), null);
        } catch (KeyManagementException | NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        return context;
    }
}
