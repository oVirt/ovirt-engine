package org.ovirt.engine.core.utils.ssh;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

import org.ovirt.engine.core.utils.crypt.OpenSSHUtils;

/**
 * SSH dialog to be used with engine defaults
 */
public class EngineSSHDialog extends SSHDialog {

    private static final Log log = LogFactory.getLog(EngineSSHDialog.class);

    /**
     * Constructor.
     */
    public EngineSSHDialog() {
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
    public void useDefaultKeyPair() throws KeyStoreException {
        final String alias = Config.<String>GetValue(ConfigValues.CertAlias);
        final String p12 = Config.<String>GetValue(ConfigValues.keystoreUrl);
        final char[] password = Config.<String>GetValue(ConfigValues.keystorePass).toCharArray();

        KeyStore.PrivateKeyEntry entry;
        InputStream in = null;
        try {
            in = new FileInputStream(p12);
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(in, password);

            entry = (KeyStore.PrivateKeyEntry)ks.getEntry(
                alias,
                new KeyStore.PasswordProtection(password)
            );
        }
        catch (Exception e) {
            throw new KeyStoreException(
                String.format(
                    "Failed to get certificate entry from key store: %1$s/%2$s",
                    p12,
                    alias
                ),
                e
            );
        }
        finally {
            Arrays.fill(password, '*');
            if (in != null) {
                try {
                    in.close();
                }
                catch(IOException e) {
                    log.error("Cannot close key store", e);
                }
            }
        }

        if (entry == null) {
            throw new KeyStoreException(
                String.format(
                    "Bad key store: %1$s/%2$s",
                    p12,
                    alias
                )
            );
        }

        setKeyPair(
            new KeyPair(
                entry.getCertificate().getPublicKey(),
                entry.getPrivateKey()
            )
        );
    }
}
