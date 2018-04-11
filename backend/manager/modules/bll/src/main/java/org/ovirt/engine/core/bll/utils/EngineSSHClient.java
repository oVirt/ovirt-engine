package org.ovirt.engine.core.bll.utils;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.uutils.ssh.OpenSSHUtils;
import org.ovirt.engine.core.uutils.ssh.SSHClient;

/**
 * SSH client to be used with engine defaults
 */
public class EngineSSHClient extends SSHClient {

    private VDS vds;

    /**
     * Constructor.
     */
    public EngineSSHClient() {
        super();
        setHardTimeout(
                TimeUnit.SECONDS.toMillis(Config.<Integer>getValue(ConfigValues.SSHInactivityHardTimeoutSeconds)));
        setSoftTimeout(
                TimeUnit.SECONDS.toMillis(Config.<Integer>getValue(ConfigValues.SSHInactivityTimeoutSeconds)));
    }

    public void setVds(VDS vds) {
        this.vds = vds;
        if (this.vds != null) {
            setHost(this.vds.getHostName(), this.vds.getSshPort());
            setUser(this.vds.getSshUsername());
        }
    }

    public VDS getVds() {
        return vds;
    }

    @Override
    public void connect() throws Exception {
        super.connect();
        if (vds != null) {
            if (StringUtils.isEmpty(vds.getSshKeyFingerprint())) {
                vds.setSshKeyFingerprint(getHostFingerprint());
                try {
                    Injector.get(VdsStaticDao.class).update(vds.getStaticData());
                } catch (Exception e) {
                    throw new SecurityException(
                            String.format(
                                "Couldn't store fingerprint to db for host %s: %s",
                                vds.getHostName(),
                                e
                         )
                     );
                }
            } else {
                StringBuilder actual = new StringBuilder();
                if (!OpenSSHUtils.checkKeyFingerprint(vds.getSshKeyFingerprint(), getHostKey(), actual)) {
                    throw new GeneralSecurityException(
                        String.format(
                            "Invalid fingerprint %s, expected %s",
                            actual,
                            vds.getSshKeyFingerprint()
                        )
                    );
                }
            }
        }
    }

    public String getHostFingerprint(String digest) throws Exception {
        return OpenSSHUtils.getKeyFingerprint(
            getHostKey(),
            digest == null ? Config.getValue(ConfigValues.SSHDefaultKeyDigest) : digest
        );
    }

    public String getHostFingerprint() throws Exception {
        return getHostFingerprint(null);
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
