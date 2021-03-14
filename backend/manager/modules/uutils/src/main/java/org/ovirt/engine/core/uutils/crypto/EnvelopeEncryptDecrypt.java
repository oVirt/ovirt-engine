package org.ovirt.engine.core.uutils.crypto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.codec.binary.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class EnvelopeEncryptDecrypt {

    private static final String ARTIFACT = "EnvelopeEncryptDecrypt";
    private static final String VERSION = "1";
    private static final String PUBKEY_DIGEST_ALGO = "SHA-1";
    private static final String PKEY_MODE_PADDING = "ECB/PKCS1Padding";

    private static final String CONTENT_KEY = "content";
    private static final String RANDOM_KEY = "random";

    private static final String ARTIFACT_KEY = "artifact";
    private static final String VERSION_KEY = "version";
    private static final String CIPHER_ALGO_KEY = "cipherAlgo";
    private static final String ENCRYPTED_CONTENT_KEY = "encryptedContent";
    private static final String IV_KEY = "iv";
    private static final String WRAPPED_KEY_KEY = "wrappedKey";
    private static final String WRAP_ALGO_KEY = "wrapAlgo";
    private static final String WRAP_KEY_DIGEST_ALGO_KEY = "wrapKeyDigestAlgo";
    private static final String WRAP_KEY_DIGEST_KEY = "wrapKeyDigest";

    private static final Random random = new SecureRandom();

    /**
     * Encrypt a content using envelope.
     *
     * @param algo
     *            Cipher algorithm to use.
     * @param bits
     *            Size of cipher key.
     * @param cert
     *            Certificate to encrypt to (wrap key using public key).
     * @param blockSize
     *            Adjust the size of content to blockSize.
     * @param content
     *            Content to encrypt.
     * @return Base64 value of envelope.
     *         <p>
     *         The blockSize is used in order to hide actual content size.
     */
    public static String encrypt(
            String algo,
            int bits,
            Certificate cert,
            int blockSize,
            byte[] content) throws GeneralSecurityException, IOException {
        final String wrapAlgo = cert.getPublicKey().getAlgorithm() + "/" + PKEY_MODE_PADDING;
        final Base64 base64 = new Base64(0);
        final Map<String, String> map = new HashMap<>();
        final Map<String, String> env = new HashMap<>();

        env.put(CONTENT_KEY, base64.encodeToString(content));
        byte[] r = new byte[((content.length / blockSize) + 1) * blockSize - content.length];
        random.nextBytes(r);
        env.put(RANDOM_KEY, base64.encodeToString(r));

        KeyGenerator gen = KeyGenerator.getInstance(algo.split("/", 2)[0]);
        gen.init(bits);
        Key key = gen.generateKey();
        Cipher cipher = Cipher.getInstance(algo);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        Cipher wrap = Cipher.getInstance(wrapAlgo);
        wrap.init(Cipher.WRAP_MODE, cert);

        map.put(ARTIFACT_KEY, ARTIFACT);
        map.put(VERSION_KEY, VERSION);
        map.put(WRAP_ALGO_KEY, wrapAlgo);
        map.put(CIPHER_ALGO_KEY, algo);
        map.put(ENCRYPTED_CONTENT_KEY,
                base64.encodeToString(cipher.doFinal(new ObjectMapper().writeValueAsString(env)
                        .getBytes(
                                StandardCharsets.UTF_8))));
        map.put(IV_KEY, base64.encodeToString(cipher.getIV()));
        map.put(WRAPPED_KEY_KEY, base64.encodeToString(wrap.wrap(key)));
        map.put(WRAP_KEY_DIGEST_ALGO_KEY, PUBKEY_DIGEST_ALGO);
        map.put(WRAP_KEY_DIGEST_KEY,
                base64.encodeToString(
                        MessageDigest.getInstance(PUBKEY_DIGEST_ALGO).digest(cert.getPublicKey().getEncoded())));
        return base64.encodeToString(new ObjectMapper().writeValueAsString(map).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decrypt a content using envelope.
     *
     * @param pkeyEntry
     *            A private key entry (key and certificate) to use for decryption.
     * @param blob
     *            value of envelope.
     * @return content.
     */
    public static byte[] decrypt(
            KeyStore.PrivateKeyEntry pkeyEntry,
            String blob) throws GeneralSecurityException, IOException {
        final Map<String, String> map = new ObjectMapper().readValue(
                Base64.decodeBase64(blob),
                TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class));

        if (!ARTIFACT.equals(map.get(ARTIFACT_KEY))) {
            throw new IllegalArgumentException(String.format("Invalid artifact '%s'", map.get(ARTIFACT_KEY)));
        }
        if (!VERSION.equals(map.get(VERSION_KEY))) {
            throw new IllegalArgumentException(String.format("Invalid version '%s'", map.get(VERSION_KEY)));
        }

        if (pkeyEntry.getCertificate() != null) {
            if (!MessageDigest.isEqual(
                    Base64.decodeBase64(map.get(WRAP_KEY_DIGEST_KEY)),
                    MessageDigest.getInstance(map.get(WRAP_KEY_DIGEST_ALGO_KEY))
                            .digest(pkeyEntry.getCertificate().getPublicKey().getEncoded()))) {
                throw new KeyException("Private key entry mismatch");
            }
        }

        Cipher wrap = Cipher.getInstance(map.get(WRAP_ALGO_KEY));
        wrap.init(Cipher.UNWRAP_MODE, pkeyEntry.getPrivateKey());
        Cipher cipher = Cipher.getInstance(map.get(CIPHER_ALGO_KEY));
        cipher.init(
                Cipher.DECRYPT_MODE,
                wrap.unwrap(
                        Base64.decodeBase64(map.get(WRAPPED_KEY_KEY)),
                        cipher.getAlgorithm().split("/", 2)[0],
                        Cipher.SECRET_KEY),
                new IvParameterSpec(Base64.decodeBase64(map.get(IV_KEY))));

        final Map<String, String> env = new ObjectMapper().readValue(
                cipher.doFinal(Base64.decodeBase64(map.get(ENCRYPTED_CONTENT_KEY))),
                TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class));
        return Base64.decodeBase64(env.get(CONTENT_KEY));
    }

}
