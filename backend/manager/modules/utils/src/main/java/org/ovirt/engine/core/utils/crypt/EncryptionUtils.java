package org.ovirt.engine.core.utils.crypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EncryptionUtils {
    private static String algo = "RSA";
    private static String certType = "PKCS12";
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
    private static String encrypt(String source, Certificate cert) throws GeneralSecurityException, UnsupportedEncodingException {
        Cipher rsa = Cipher.getInstance(algo);
        rsa.init(Cipher.ENCRYPT_MODE, cert.getPublicKey());
        return new Base64(0).encodeToString(
            rsa.doFinal(source.trim().getBytes("UTF-8"))
        );
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
     * @param alias
     *            The certificate alias.
     * @return
     *
     *         Loads a private key from a PKCS#12 file using the supplied password and encrypts the value. The string which
     *         is returned is first base64 encoded The keyfile is loaded first off of the classpath, then from the file
     *         system.
     */
    public final static String encrypt(String source, String certificateFile, String passwd, String alias)
            throws Exception {
        try {
            String result = "";
            if (source.trim().length() > 0) {
                KeyStore store = EncryptionUtils.getKeyStore(certificateFile, passwd, certType);
                result = encrypt(source, store.getCertificate(alias));
            }
            return result;
        } catch (Exception e) {
            log.error("Error doing the encryption", e);
            throw e;
        }
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
     *         Loads a private key from a PKCS#12 file using the supplied password and decrypts the value. The string to be
     *         decrypted is assumed to have been base64 encoded. The keyfile is loaded from the file system
     */
    public static String decrypt(String source, String keyFile, String passwd, String alias)
            throws Exception {
        try {
            String result = "";
            if (source.trim().length() > 0) {
                KeyStore store = EncryptionUtils.getKeyStore(keyFile, passwd, certType);
                result = decrypt(source, store.getKey(alias, passwd.toCharArray()));
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to decrypt " + e.getMessage());
            log.debug("Failed to decrypt", e);
            throw e;
        }
    }

    private static String decrypt(String source, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        Cipher rsa = Cipher.getInstance(algo);
        rsa.init(Cipher.DECRYPT_MODE, key);
        return new String(
            rsa.doFinal(Base64.decodeBase64(source)),
            "UTF-8"
        );
    }

    public static KeyStore getKeyStore(String path, String passwd, String storeType) {
        File storeFile = new File(path);
        InputStream storeIn = null;
        try {
            storeIn = new FileInputStream(storeFile);
            KeyStore store = KeyStore.getInstance(storeType);
            store.load(storeIn, passwd.toCharArray());
            return store;
        } catch (Exception exception) {
            log.error(String.format("Can't load keystore from file \"%s\". %s: %s",
                    storeFile.getAbsolutePath(),
                    exception.getClass().getSimpleName(),
                    exception.getMessage()));
            throw new RuntimeException(exception);
        }
        finally {
            if (storeIn != null) {
                try {
                    storeIn.close();
                }
                catch (IOException exception) {
                    log.warn("Can't close keystore file \"" + storeFile.getAbsolutePath() + "\".", exception);
                }
            }
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
            encoding = cipher.doFinal(secret.getBytes("UTF-8"));
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
            return decode != null ? new String(decode, "UTF-8") : null;
        } catch (Exception e) {
            log.error("Error in decrypting the secret", e);
            return null;
        }
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

        public EncryptUtilParams(String keyMaterial, String algorithm) throws UnsupportedEncodingException {
            if (algorithm == null || "".equals(algorithm)) {
                this.algorithm = "Blowfish";
            } else {
                this.algorithm = algorithm;
            }

            if (keyMaterial == null || "".equals(keyMaterial)) {
                secretKey = new SecretKeySpec("jaas is the way".getBytes("UTF-8"), this.algorithm);
            } else {
                secretKey = new SecretKeySpec(keyMaterial.getBytes(), this.algorithm);
            }
        }
    }

}
