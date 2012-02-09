package org.ovirt.engine.core.engineencryptutils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.Enumeration;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ovirt.engine.core.compat.Encoding;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringHelper;

public class EncryptionUtils {
    private static String algo = "RSA";
    private static String certType = "JKS";
    private static Log log = LogFactory.getLog(EncryptionUtils.class);

    /**
     * Encrypts the specified source.
     *
     * @param source
     *            The source.
     * @param cert
     *            The cert.
     * @return base64 encoded result.
     */
    private static String encrypt(String source, Certificate cert) throws GeneralSecurityException {
        String result = null;
        byte[] cipherbytes = Encoding.UTF8.getBytes(source.trim());
        Cipher rsa = Cipher.getInstance(algo);
        rsa.init(Cipher.ENCRYPT_MODE, cert.getPublicKey());
        byte[] cipher = rsa.doFinal(cipherbytes);
        result = Encoding.Base64.getString(cipher);
        return result;
    }

    /**
     * Decrypts the specified source given a Private Key
     *
     * @param source
     *            The source.
     * @param key
     *            The private key.
     * @param error
     *            The error.
     * @return
     */
    private static String Decrypt(String source, Key key, RefObject<String> error) {
        error.argvalue = "";
        String result = "";
        try {
            {
                byte[] cipherbytes = Encoding.Base64.getBytes(source);
                {
                    Cipher rsa = Cipher.getInstance(algo);
                    rsa.init(Cipher.DECRYPT_MODE, key);
                    {
                        byte[] plainbytes = rsa.doFinal(cipherbytes);
                        result = Encoding.ASCII.getString(plainbytes);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in the Decryption", e);
            error.argvalue = e.getMessage();
        }
        return result;
    }

    /**
     * Encrypts the specified source using certification file.
     *
     * @param source
     *            The source.
     * @param certificateFile
     *            The certificate file.
     * @param passwd
     *            The passwd.
     * @param alis
     *            The certificate alias.
     * @return
     *
     *         Loads a private key from a JKS file using the supplied password and encrypts the value. The string which
     *         is returned is first base64 encoded The keyfile is loaded first off of the classpath, then from the file
     *         system.
     */
    public final static String encrypt(String source, String certificateFile, String passwd, String alias)
            throws Exception {
        String result = "";
        if (!StringHelper.isNullOrEmpty(source.trim())) {
            try {
                KeyStore store = EncryptionUtils.getKeyStore(certificateFile, passwd, certType);
                Certificate cert = store.getCertificate(alias);
                result = encrypt(source, cert);
            } catch (Exception e) {
                log.error("Error doing the encryption", e);
                throw e;
            }
        }
        return result;
    }

    /**
     * Decrypts the specified source using certification file.
     *
     * @param source
     *            The source.
     * @param keyFile
     *            The key file.
     * @param passwd
     *            The passwd.
     * @param alias
     *            The certificate alias.
     * @return
     *
     *         Loads a private key from a JKS file using the supplied password and decrypts the value. The string to be
     *         decrypted is assumed to have been base64 encoded. The keyfile is loaded from the file system
     */
    public static String decrypt(String source, String keyFile, String passwd, String alias)
            throws Exception {
        String result = source;
        try {
            if (!StringHelper.isNullOrEmpty(source.trim())) {
                KeyStore store = EncryptionUtils.getKeyStore(keyFile, passwd, certType);
                Key key = store.getKey(alias, passwd.toCharArray());
                result = decrypt(source, key);
            }
        } catch (Exception e) {
            log.error("Failed to decrypt" + e.getMessage());
            log.debug("Failed to decrypt", e);
            throw e;
        }
        return result;
    }

    private static String decrypt(String source, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String result = "";
        byte[] cipherbytes = Encoding.Base64.getBytes(source);
        Cipher rsa = Cipher.getInstance(algo);
        rsa.init(Cipher.DECRYPT_MODE, key);
        byte[] plainbytes = rsa.doFinal(cipherbytes);
        result = Encoding.UTF8.getString(plainbytes);
        return result;
    }

    /**
     * Decrypts the specified source using Store.
     *
     * @param source
     *            The source.
     * @param certificateFingerPrint
     *            The certification finger print.
     * @param error
     *            The error.
     * @return
     */
    public static final String Decrypt(String source, String keyFile, String passwd,
            RefObject<String> error) {
        error.argvalue = "";
        String result = source;
        if (!StringHelper.isNullOrEmpty(source.trim())) {
            try {
                KeyStore store = EncryptionUtils.getKeyStore(keyFile, passwd, certType);

                // Get the first one.
                String alias = "None";
                Enumeration<String> aliases = store.aliases();
                while (aliases.hasMoreElements()) {
                    alias = aliases.nextElement();
                    break;
                }
                Key key = store.getKey(alias, passwd.toCharArray());
                result = Decrypt(source, key, error);
            } catch (Exception e) {
                log.error("Error doing the decryption", e);
                error.argvalue = e.getMessage();
            }
        }
        return result;
    }

    /**
     * Determines whether the specified name is a password holder.
     *
     * @param name
     *            The name.
     * @return <c>true</c> if the specified name is password; otherwise, <c>false</c>.
     */
    public static boolean IsPassword(String name) {
        final String PASSWORD = "NoSoup4U";
        return (name.toLowerCase().endsWith(PASSWORD));
    }

    public static KeyStore getKeyStore(String path, String passwd, String storeType) {
        try {
            InputStream fis = new FileInputStream(path);
            KeyStore store = KeyStore.getInstance(storeType);
            store.load(fis, passwd.toCharArray());
            return store;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Encodes a given secret by specified key material and algorithm<BR>
     * If not supplying any key material and algorithm using defaults:<BR>
     * keyMaterial - <i>"jaas is the way"</i> <BR>
     * algorithm - <i>"Blowfish"<i>
     *
     * @param secret
     *            the encrypted object
     * @param keyMaterial
     *            the material for the encryption
     * @param algorithm
     *            defines which algorithm to use
     * @return the encrypted secret or null if encryption fails
     */
    public static String encode(String secret, String keyMaterial, String algorithm) {
        Cipher cipher;
        byte[] encoding = null;
        try {
            EncryptUtilParams params = new EncryptUtilParams(keyMaterial, algorithm);
            cipher = Cipher.getInstance(params.algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, params.secretKey);
            encoding = cipher.doFinal(secret.getBytes());
        } catch (Exception e) {
            log.error("Error in encrypting the secret", e);
        }

        return encoding != null ? (new BigInteger(encoding)).toString(16) : null;

    }

    /**
     * Decodes a given secret by specified key material and algorithm.<BR>
     * If no key material and algorithm are supplied, using defaults:<BR>
     * keyMaterial - <i>"jaas is the way"</i> <BR>
     * algorithm - <i>"Blowfish"<i>
     *
     * @param secret
     *            the object to decode
     * @param keyMaterial
     *            the material for the decoding action
     * @param algorithm
     *            defines which algorithm to use
     * @return the decoded secret or null if decode fails
     */
    public static String decode(String secret, String keyMaterial, String algorithm) {

        BigInteger n = new BigInteger(secret, 16);
        byte[] encoding = n.toByteArray();
        byte[] decode = null;
        try {
            EncryptUtilParams params = new EncryptUtilParams(keyMaterial, algorithm);
            Cipher cipher = Cipher.getInstance(params.algorithm);
            cipher.init(Cipher.DECRYPT_MODE, params.secretKey);
            decode = cipher.doFinal(encoding);
        } catch (Exception e) {
            log.error("Error in decrypting the secret", e);
        }
        return decode != null ? new String(decode) : null;
    }

    /**
     * Class use to holds and defines defaults values for encryption/decryption <BR>
     * based on. Targeted to set defaults values as used in JBoss security login module:<BR>
     * keyMaterial - <i>"jaas is the way"</i> <BR>
     * algorithm - <i>"Blowfish"<i>
     */
    private static class EncryptUtilParams {
        private String algorithm = null;
        private SecretKeySpec secretKey = null;

        public EncryptUtilParams(String keyMaterial, String algorithm) {
            if (algorithm == null || "".equals(algorithm)) {
                this.algorithm = "Blowfish";
            } else {
                this.algorithm = algorithm;
            }

            if (keyMaterial == null || "".equals(keyMaterial)) {
                secretKey = new SecretKeySpec("jaas is the way".getBytes(), this.algorithm);
            } else {
                secretKey = new SecretKeySpec(keyMaterial.getBytes(), this.algorithm);
            }
        }
    }

}
