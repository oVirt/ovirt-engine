package org.ovirt.engine.core.utils.crypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.codec.binary.Base64;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.uutils.ssh.OpenSSHUtils;

public class EngineEncryptionUtils {
    private static final String keystoreType;
    private static final File keystoreFile;
    private static final KeyStore.PasswordProtection keystorePassword;
    private static final String keystoreAlias;

    private static final String truststoreType;
    private static final File truststoreFile;
    private static final KeyStore.PasswordProtection truststorePassword;

    static {
        EngineLocalConfig config = EngineLocalConfig.getInstance();
        keystoreType = config.getPKIEngineStoreType();
        keystoreFile = config.getPKIEngineStore().getAbsoluteFile();
        keystorePassword = new KeyStore.PasswordProtection(config.getPKIEngineStorePassword().toCharArray());
        keystoreAlias = config.getPKIEngineStoreAlias();
        truststoreType = config.getPKITrustStoreType();
        truststoreFile = config.getPKITrustStore().getAbsoluteFile();
        truststorePassword = new KeyStore.PasswordProtection(config.getPKITrustStorePassword().toCharArray());
    }

    /**
     * Return the engine keystore.
     * @return engine key store.
     */
    private static KeyStore getKeyStore(String type, File file, char[] password) {
        try (final InputStream in = new FileInputStream(file)) {
            KeyStore ks = KeyStore.getInstance(type);
            ks.load(in, password);
            return ks;
        } catch (Exception e) {
            throw new RuntimeException(
                String.format(
                    "Failed to local keystore '%1$s'",
                    file
                ),
                e
            );
        }
    }

    /**
     * Return the engine keystore.
     * @return engine key store.
     */
    public static KeyStore getKeyStore() {
        return getKeyStore(keystoreType, keystoreFile, keystorePassword.getPassword());
    }

    /**
     * Return the trust keystore.
     * @return engine key store.
     */
    public static KeyStore getTrustStore() {
        return getKeyStore(truststoreType, truststoreFile, truststorePassword.getPassword());
    }

    /**
     * Return the engine private key entry.
     * @return private key entry.
     */
    public static KeyStore.PrivateKeyEntry getPrivateKeyEntry() {
        try {
            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry)getKeyStore().getEntry(
                keystoreAlias,
                keystorePassword
            );
            if (entry == null) {
                throw new RuntimeException("Alias was not found");
            }
            return entry;
        } catch(Exception e) {
            throw new RuntimeException(
                String.format(
                    "Failed to locate key '%1$s'",
                    keystoreAlias
                ),
                e
            );
        }
    }

    /**
     * Return the engine certificate.
     * @return certificate key.
     */
    public static Certificate getCertificate() {
        return getPrivateKeyEntry().getCertificate();
    }


    public static X509Certificate getCertificate(File file) {
        try (InputStream in = new FileInputStream(file)) {
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
        } catch (CertificateException | IOException e) {
            throw new RuntimeException(
                    String.format(
                            "Failed to read certificate '%1$s'",
                            file.getName()
                    ),
                    e
            );
        }
    }

    /**
     * Return the engine ssh public key representation.
     * @return ssh public key.
     */
    public static String getEngineSSHPublicKey() {
        return OpenSSHUtils.getKeyString(
            getCertificate().getPublicKey(),
            Config.getValue(ConfigValues.SSHKeyAlias)
        );
    }

    /**
     * Encrypt string.
     * @param source string to encrypt.
     * @return encrypted string.
     * @throws GeneralSecurityException
     * Please note that empty strings are not encrypted and are returned as-is.
     */
    public static String encrypt(String source) throws GeneralSecurityException {
        if (source == null || source.length() == 0) {
            return source;
        } else {
            Cipher rsa = Cipher.getInstance("RSA");
            rsa.init(Cipher.ENCRYPT_MODE, getCertificate().getPublicKey());
            return new Base64(0).encodeToString(
                rsa.doFinal(source.getBytes(StandardCharsets.UTF_8))
            );
        }
    }

    /**
     * Decrypt string.
     * @param source string to decrypt.
     * @return decrypted string.
     * @throws GeneralSecurityException
     * Please note that empty strings are not decrypted and are returned as-is.
     */
    public static String decrypt(String source) throws GeneralSecurityException {
        if (source == null || source.length() == 0) {
            return source;
        } else {
            Cipher rsa = Cipher.getInstance("RSA");
            rsa.init(Cipher.DECRYPT_MODE, getPrivateKeyEntry().getPrivateKey());
            return new String(
                rsa.doFinal(new Base64().decode(source)),
                StandardCharsets.UTF_8
            );
        }
    }

    /**
     * Return key managers.
     * @return array of key managers.
     */
    public static KeyManager[] getKeyManagers() throws GeneralSecurityException {
        KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmfactory.init(getKeyStore(), keystorePassword.getPassword());
        return kmfactory.getKeyManagers();
    }

    /**
     * Return trust managers.
     * @return array of trust managers.
     */
    public static TrustManager[] getTrustManagers() throws GeneralSecurityException {
        TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmfactory.init(getTrustStore());
        return tmfactory.getTrustManagers();
    }

    /**
     * Check if we have key.
     * @return true if we do.
     * Have no idea why this is required.
     */
    public static boolean haveKey() {
        try {
            getPrivateKeyEntry();
            return true;
        } catch(Exception e) {
            return false;
        }
    }
}
