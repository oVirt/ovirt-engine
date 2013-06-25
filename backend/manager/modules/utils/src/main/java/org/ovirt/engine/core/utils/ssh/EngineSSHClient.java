package org.ovirt.engine.core.utils.ssh;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.utils.crypt.OpenSSHUtils;

/**
 * SSH client to be used with engine defaults
 */
public class EngineSSHClient extends SSHClient {

    private static final Log log = LogFactory.getLog(EngineSSHDialog.class);

    /**
     * Constructor.
     */
    public EngineSSHClient() {
        super();
        setHardTimeout(
            Config.<Integer>GetValue(
                ConfigValues.SSHInactivityHardTimoutSeconds
            ) * 1000
        );
        setSoftTimeout(
            Config.<Integer>GetValue(
                ConfigValues.SSHInactivityTimoutSeconds
            ) * 1000
        );
    }

    /**
     * Get host fingerprint.
     * @return fingerprint.
     */
    public String getHostFingerprint() throws IOException {
        String fingerprint = OpenSSHUtils.getKeyFingerprintString(getHostKey());

        if (fingerprint == null) {
            throw new IOException("Unable to parse host key");
        }

        return fingerprint;
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
