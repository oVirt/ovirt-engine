package org.ovirt.engine.core.bll.utils;

import java.net.ConnectException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.uutils.ssh.SSHClient;
import org.ovirt.engine.core.uutils.ssh.SSHDialog;

/**
 * SSH dialog to be used with engine defaults
 */
public class EngineSSHDialog extends SSHDialog {

    private VDS vds;

    public EngineSSHDialog() {
        this(TimeUnit.SECONDS.toMillis(Config.<Integer>getValue(ConfigValues.SSHInactivityTimeoutSeconds)),
                TimeUnit.SECONDS.toMillis(Config.<Integer>getValue(ConfigValues.SSHInactivityHardTimeoutSeconds)));
    }

    public EngineSSHDialog(long softTimeout, long hardTimeout) {
        setSoftTimeout(softTimeout);
        setHardTimeout(softTimeout > hardTimeout ? softTimeout : hardTimeout);
    }

    @Override
    protected SSHClient getSSHClient() {
        EngineSSHClient client = new EngineSSHClient();
        client.setVds(vds);
        return client;
    }

    /**
     * Setting internal vds object
     */
    public void setVds(VDS vds) throws Exception {
        this.vds = vds;
        setHost(this.vds.getHostName(), this.vds.getSshPort());
        setUser(this.vds.getSshUsername());
    }

    /**
     * Get host fingerprint.
     * @return fingerprint.
     */
    public String getHostFingerprint() throws Exception {
        if (client == null) {
            throw new ConnectException("SSH is not connected");
        }
        return ((EngineSSHClient) client).getHostFingerprint();
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
