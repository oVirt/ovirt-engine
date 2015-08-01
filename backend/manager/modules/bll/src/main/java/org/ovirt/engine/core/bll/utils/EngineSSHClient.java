package org.ovirt.engine.core.bll.utils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;

import org.apache.commons.lang.StringUtils;
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
            String expected = _vds.getSshKeyFingerprint();

            String hash;
            if (StringUtils.isEmpty(expected)) {
                hash = "SHA-256";
            } else {
                expected = OpenSSHUtils.fixupKeyFingerprintHash(expected);
                hash = OpenSSHUtils.getKeyFingerprintHash(expected);
            }

            String actual = getHostFingerprint(hash);

            if (StringUtils.isEmpty(expected)) {
                _vds.setSshKeyFingerprint(actual);
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

    public String getHostFingerprint(String hash) throws IOException {
        String fingerprint = OpenSSHUtils.getKeyFingerprint(getHostKey(), hash);

        if (fingerprint == null) {
            throw new IOException("Unable to parse host key");
        }

        return fingerprint;
    }

    public String getHostFingerprint() throws IOException {
        return getHostFingerprint("SHA-256");
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
