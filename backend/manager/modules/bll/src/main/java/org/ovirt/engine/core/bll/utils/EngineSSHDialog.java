package org.ovirt.engine.core.bll.utils;

import java.io.IOException;
import java.net.ConnectException;
import java.security.KeyPair;
import java.security.KeyStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.utils.ssh.SSHClient;
import org.ovirt.engine.core.utils.ssh.SSHDialog;
import org.ovirt.engine.core.common.businessentities.VDS;

/**
 * SSH dialog to be used with engine defaults
 */
public class EngineSSHDialog extends SSHDialog {

    private static final Log log = LogFactory.getLog(EngineSSHDialog.class);
    VDS _vds;

    protected SSHClient _getSSHClient() {
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
    public String getHostFingerprint() throws IOException {
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
