package org.ovirt.engine.core.bll.utils;

import java.net.ConnectException;
import java.security.KeyPair;
import java.security.KeyStore;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.uutils.ssh.SSHClient;
import org.ovirt.engine.core.uutils.ssh.SSHDialog;

/**
 * SSH dialog to be used with engine defaults
 */
public class EngineSSHDialog extends SSHDialog {

    VDS _vds;

    @Override
    protected SSHClient getSSHClient() {
        EngineSSHClient client = new EngineSSHClient();
        client.setVds(_vds);
        return client;
    }

    /**
     * Setting internal vds object
     */
    public void setVds(VDS vds) throws Exception {
        _vds = vds;
        setHost(_vds.getHostName(), _vds.getSshPort());
        setUser(_vds.getSshUsername());
    }

    /**
     * Get host fingerprint.
     * @return fingerprint.
     */
    public String getHostFingerprint() throws Exception {
        if (_client == null) {
            throw new ConnectException("SSH is not connected");
        }
        return ((EngineSSHClient)_client).getHostFingerprint();
    }

    /**
     * Use default engine ssh key.
     */
    public void useDefaultKeyPair() {
        KeyStore.PrivateKeyEntry entry = EngineEncryptionUtils.getPrivateKeyEntry();

        setKeyPair(
            new KeyPair(
                entry.getCertificate().getPublicKey(),
                entry.getPrivateKey()
            )
        );
    }
}
