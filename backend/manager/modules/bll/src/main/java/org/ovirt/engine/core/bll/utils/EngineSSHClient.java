package org.ovirt.engine.core.bll.utils;

import static org.ovirt.engine.core.uutils.ssh.OpenSSHUtils.getKeyString;

import java.io.IOException;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PublicKey;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.sshd.client.keyverifier.ServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.config.keys.PublicKeyEntryResolver;
import org.apache.sshd.common.signature.BuiltinSignatures;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.uutils.ssh.OpenSSHUtils;
import org.ovirt.engine.core.uutils.ssh.SSHClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SSH client to be used with engine defaults
 */
public class EngineSSHClient extends SSHClient {

    private static final Logger log = LoggerFactory.getLogger(EngineSSHClient.class);

    private final AtomicReference<VDS> vdsHolder = new AtomicReference<>();
    private final AtomicReference<PublicKey> hostPublicKeyHolder = new AtomicReference<>();

    /**
     * Constructor.
     */
    public EngineSSHClient() {
        super();
        setServerKeyVerifier(new OvirtSshKeyVerifier(vdsHolder, hostPublicKeyHolder));
        setHardTimeout(
                TimeUnit.SECONDS.toMillis(Config.<Integer> getValue(ConfigValues.SSHInactivityHardTimeoutSeconds)));
        setSoftTimeout(
                TimeUnit.SECONDS.toMillis(Config.<Integer> getValue(ConfigValues.SSHInactivityTimeoutSeconds)));
    }

    @Override
    public void connect() throws Exception {
        configureSupportedSSHPublicKeySignatures();
        super.connect();
    }

    public void setVds(VDS vds) {
        if (vds != null) {
            this.vdsHolder.set(vds);
            setHost(vds.getHostName(), vds.getSshPort());
            setUser(vds.getSshUsername());
        }
    }

    public String getHostPublicKey() {
        return OpenSSHUtils.getKeyString(hostPublicKeyHolder.get(), "");
    }

    /**
     * Use default engine ssh key.
     */
    public void useDefaultKeyPair() {
        KeyStore.PrivateKeyEntry entry = EngineEncryptionUtils.getPrivateKeyEntry();

        setKeyPair(
                new KeyPair(
                        entry.getCertificate().getPublicKey(),
                        entry.getPrivateKey()));
    }

    private void configureSupportedSSHPublicKeySignatures() {
        VDS vds = vdsHolder.get();
        if (vds != null) {
            if (StringUtils.isNotBlank(vds.getSshKeyFingerprint())) {
                if (StringUtils.isBlank(vds.getSshPublicKey())) {
                    // backward compatibility:
                    // when no public key then assume it was RSA from which fingerprint was calculated
                    // ask host's ssh server for RSA only
                    addExpectedSignatures(BuiltinSignatures.rsaSHA512,
                            BuiltinSignatures.rsaSHA256,
                            BuiltinSignatures.rsa);
                } else {
                    // Public key is known, ask host's ssh server for only that type of key to check
                    AuthorizedKeyEntry entry =
                            AuthorizedKeyEntry.parseAuthorizedKeyEntry(vds.getSshPublicKey());
                    final BuiltinSignatures allowedSignature = BuiltinSignatures.fromFactoryName(entry.getKeyType());
                    if (allowedSignature != null) {
                        addExpectedSignatures(allowedSignature);
                    } else {
                        log.warn(
                                "Unknown signature {} for host {} public key {}. " +
                                        "Falling back to whatever host's ssh server will provide",
                                entry.getKeyType(),
                                vds.getHostName(),
                                vds.getSshPublicKey());
                    }
                }
            }
        }

        if (isKeyPairSet() && isAtLeastOneExpectedSignatureSet()) {
            // Engine's key signature MUST be included and because by default it is still RSA
            // so it MUST be on the last place because apparently the order matters
            // note that if no restrictions are set earlier it falls back to default signatures
            final String enginePublicKeyType =
                    AuthorizedKeyEntry.parseAuthorizedKeyEntry(EngineEncryptionUtils.getEngineSSHPublicKey())
                            .getKeyType();
            addExpectedSignatures(BuiltinSignatures.fromFactoryName(enginePublicKeyType));
        }
    }

    public static class OvirtSshKeyVerifier implements ServerKeyVerifier {

        private final AtomicReference<VDS> vdsHolder;
        private final AtomicReference<PublicKey> hostPublicKeyHolder;

        public OvirtSshKeyVerifier(AtomicReference<VDS> vdsHolder, AtomicReference<PublicKey> hostPublicKeyHolder) {
            this.hostPublicKeyHolder = hostPublicKeyHolder;
            this.vdsHolder = vdsHolder;

        }

        @Override
        public boolean verifyServerKey(ClientSession clientSession, SocketAddress remoteAddress, PublicKey serverKey) {
            if (vdsHolder.get() != null) {
                if (StringUtils.isBlank(vdsHolder.get().getSshKeyFingerprint())) {
                    // this is a new host
                    storeHostSSHPublicKey(serverKey);
                    return true;
                }

                // Backward compatibility to handle previously added hosts
                // that had not had public keys (RSA only) stored.
                // Check only fingerprints.
                if (StringUtils.isBlank(vdsHolder.get().getSshPublicKey())) {
                    if (OpenSSHUtils.checkKeyFingerprint(vdsHolder.get().getSshKeyFingerprint(), serverKey)) {
                        // update public key with RSA server key
                        storeHostSSHPublicKey(serverKey);
                        return true;
                    }
                    return false;
                }

                // compare server public key with one already stored in engine's db
                return checkPublicKey(serverKey);
            }
            hostPublicKeyHolder.set(serverKey);
            return true;
        }

        private void storeHostSSHPublicKey(PublicKey serverKey) {
            String fingerprint = OpenSSHUtils.getKeyFingerprint(
                    serverKey,
                    Config.getValue(ConfigValues.SSHDefaultKeyDigest));
            vdsHolder.get().setSshKeyFingerprint(fingerprint);
            vdsHolder.get().setSshPublicKey(getKeyString(serverKey, null));
            try {
                Injector.get(VdsStaticDao.class).update(vdsHolder.get().getStaticData());
            } catch (Exception e) {
                throw new SecurityException(
                        String.format(
                                "Couldn't store fingerprint or public key to db for host %s: %s",
                                vdsHolder.get().getHostName(),
                                e));
            }

        }

        private boolean checkPublicKey(PublicKey serverKey) {
            AuthorizedKeyEntry entry = AuthorizedKeyEntry.parseAuthorizedKeyEntry(vdsHolder.get().getSshPublicKey());
            try {
                PublicKey expectedPublicKey =
                        entry.resolvePublicKey(null, Collections.emptyMap(), PublicKeyEntryResolver.FAILING);
                return KeyUtils.compareKeys(expectedPublicKey, serverKey);
            } catch (IOException | GeneralSecurityException e) {
                log.error("Error comparing ssh public keys: {}", ExceptionUtils.getRootCauseMessage(e));
                log.debug("Error comparing ssh public keys", e);
                return false;
            }
        }

    }

}
