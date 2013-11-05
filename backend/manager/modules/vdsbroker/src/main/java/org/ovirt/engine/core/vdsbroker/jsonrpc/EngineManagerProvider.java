package org.ovirt.engine.core.vdsbroker.jsonrpc;

import java.security.GeneralSecurityException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.vdsm.jsonrpc.client.reactors.ManagerProvider;

/**
 * Engine specific implementation of <code>ManagerProvider</code>
 * which provides <code>KeyManager</code>s and <code>TrustManager</code>.
 *
 */
public class EngineManagerProvider extends ManagerProvider {

    @Override
    public KeyManager[] getKeyManagers() throws GeneralSecurityException {
        return EngineEncryptionUtils.getKeyManagers();
    }

    @Override
    public TrustManager[] getTrustManagers() throws GeneralSecurityException {
        return EngineEncryptionUtils.getTrustManagers();
    }

}
