package org.ovirt.engine.core.bll.utils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.uutils.ssh.OpenSSHUtils;
import org.ovirt.engine.core.uutils.ssh.SSHClient;

/**
 * SSH client to be used with engine defaults
 */
public class EngineSSHClient extends SSHClient {

    private VDS _vds;

    /**
     * Constructor.
     */
    public EngineSSHClient() {
        super();
        setHardTimeout(
            Config.<Integer>getValue(
                ConfigValues.SSHInactivityHardTimeoutSeconds
            ) * 1000
        );
        setSoftTimeout(
            Config.<Integer>getValue(
                ConfigValues.SSHInactivityTimeoutSeconds
            ) * 1000
        );
    }

    public void setVds(VDS vds) {
        _vds = vds;
        if (_vds != null) {
            setHost(_vds.getHostName(), _vds.getSshPort());
            setUser(_vds.getSshUsername());
        }
    }

    public VDS getVds() {
        return _vds;
    }

    @Override
    public void connect() throws Exception {
        super.connect();
        if (_vds != null) {
            String actual = getHostFingerprint();
            String expected = _vds.getSshKeyFingerprint();

            if (expected == null || expected.isEmpty()) {
                _vds.setSshKeyFingerprint(getHostFingerprint());
                try {
                    DbFacade.getInstance().getVdsStaticDao().update(_vds.getStaticData());
                } catch (Exception e) {
                    throw new SecurityException(
                            String.format(
                                "Couldn't store fingerprint to db for host %s: %s",
                                _vds.getHostName(),
                                e
                         )
                     );
                }
            } else if (!actual.equals(expected)) {
                throw new GeneralSecurityException(
                    String.format(
                        "Invalid fingerprint %s, expected %s",
                        actual,
                        expected
                    )
                );
            }
        }
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
