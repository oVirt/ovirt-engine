package org.ovirt.engine.core.uutils.ssh;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenSSHUtils {
    // The log:
    private static final Logger log = LoggerFactory.getLogger(OpenSSHUtils.class);

    // Names of supported algorithms:
    private static final String SSH_RSA = "ssh-rsa";
    private static final String MD5 = "MD5";

    private OpenSSHUtils () {
        // No instances allowed.
    }

    private static byte[] getByteArrayOfData(DataInputStream dataInputStream) throws IOException {
        byte [] contents = new byte[dataInputStream.readInt()];
        if (dataInputStream.read(contents, 0, contents.length) != contents.length) {
            throw new IOException("Invalid ASN1 array");
        }
        return contents;
    }

    /**
     * Convert a public key string to real public key.
     */
    public static PublicKey decodeKeyString(final String key) throws IOException, GeneralSecurityException {
        String[] words = key.split("\\s+", 3);

        if (words.length < 2 || !SSH_RSA.equals(words[0])) {
            throw new GeneralSecurityException("Unsupported SSH public key");
        }


        try (
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.decodeBase64(words[1]));
            DataInputStream dataInputStream = new DataInputStream(inputStream)
        ) {
            if (!Arrays.equals(getByteArrayOfData(dataInputStream), SSH_RSA.getBytes(StandardCharsets.UTF_8))) {
                throw new GeneralSecurityException("Unsupported SSH public key");
            }

            byte[] exponentBytes = getByteArrayOfData(dataInputStream);
            byte[] modulusBytes = getByteArrayOfData(dataInputStream);

            return KeyFactory.getInstance("RSA").generatePublic(
                new RSAPublicKeySpec(
                    new BigInteger(modulusBytes),
                    new BigInteger(exponentBytes)
                )
            );
        }
    }

    /**
     * Convert a public key to the SSH format.
     *
     * Note that only RSA keys are supported at the moment.
     *
     * @param key the public key to convert
     * @return an array of bytes that can with the representation of
     *   the public key
     */
    public static byte[] getKeyBytes(final PublicKey key) {
        // We only support RSA at the moment:
        if (!(key instanceof RSAPublicKey)) {
            log.error("The key algorithm '{}' is not supported, will return null.", key.getAlgorithm());
            return null;
        }

        // Extract the bytes of the exponent and the modulus
        // of the key:
        final RSAPublicKey rsaKey = (RSAPublicKey) key;
        final byte[] exponentBytes = rsaKey.getPublicExponent().toByteArray();
        final byte[] modulusBytes = rsaKey.getModulus().toByteArray();
        if (log.isDebugEnabled()) {
            log.debug("Exponent is {} ({}).", rsaKey.getPublicExponent(), Hex.encodeHexString(exponentBytes));
            log.debug("Modulus is {} ({}).", rsaKey.getModulus(), Hex.encodeHexString(exponentBytes));
        }

        try {
            // Prepare the stream to write the binary SSH key:
            final ByteArrayOutputStream binaryOut = new ByteArrayOutputStream();
            final DataOutputStream dataOut = new DataOutputStream(binaryOut);

            // Write the SSH header (4 bytes for the length of the algorithm
            // name and then the algorithm name):
            dataOut.writeInt(SSH_RSA.length());
            dataOut.writeBytes(SSH_RSA);

            // Write the exponent and modulus bytes (note that it is not
            // necessary to check if the most significative bit is one, as
            // that will never happen with byte arrays created from big
            // integers, unless they are negative, which is not the case
            // for RSA modulus or exponents):
            dataOut.writeInt(exponentBytes.length);
            dataOut.write(exponentBytes);
            dataOut.writeInt(modulusBytes.length);
            dataOut.write(modulusBytes);

            // Done, extract the bytes:
            binaryOut.close();
            final byte[] keyBytes = binaryOut.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("Key bytes are {}.", Hex.encodeHexString(keyBytes));
            }

            return keyBytes;
        }
        catch (IOException exception) {
            log.error("Error while serializing public key, will return null.", exception);
            return null;
        }
    }

    /**
     * Convert a public key to the SSH format used in the
     * <code>authorized_keys</code> files.
     *
     * Note that only RSA keys are supported at the moment.
     *
     * @param key the public key to convert
     * @param alias the alias to be appended at the end of the line, if
     *   it is <code>null</code> nothing will be appended
     * @return an string that can be directly written to the
     *   <code>authorized_keys</code> file or <code>null</code> if the
     *   conversion can't be performed for whatever the reason
     */
    public static String getKeyString(final PublicKey key, String alias) {
        // Get the serialized version of the key:
        final byte[] keyBytes = getKeyBytes(key);
        if (keyBytes == null) {
            log.error("Can't get key bytes, will return null.");
            return null;
        }

        // Encode it using BASE64:
        final Base64 encoder = new Base64(0);
        final String encoding = encoder.encodeToString(keyBytes);
        if (log.isDebugEnabled()) {
            log.debug("Key encoding is '{}'.", encoding);
        }

        // Return the generated SSH public key:
        final StringBuilder buffer = new StringBuilder(SSH_RSA.length() + 1 + encoding.length() + (alias != null? 1 + alias.length(): 0) + 1);
        buffer.append(SSH_RSA);
        buffer.append(" ");
        buffer.append(encoding);
        if (alias != null) {
            buffer.append(" ");
            buffer.append(alias);
        }
        buffer.append('\n');
        final String keyString = buffer.toString();
        if (log.isDebugEnabled()) {
            log.debug("Key string is '{}'.", keyString);
        }

        return keyString;
    }

    public static final boolean checkKeyFingerprint(String expected, final PublicKey key, StringBuilder actual) throws Exception {
        String digest = expected.split(":", 2)[0];
        try {
            if (digest.length() == 2) {
                Integer.parseInt(digest, 16);
                digest = MD5;
                expected = digest + ":" + expected;
            }
        } catch(NumberFormatException e) {
            // ignore
        }

        if (!digest.startsWith("MD")) {
            digest = digest.replaceFirst("([0-9])", "-$1");
        }

        String fingerprint = getKeyFingerprint(key, digest);

        boolean result;
        if (MD5.equals(digest)) {
            result = expected.equalsIgnoreCase(fingerprint);
        } else {
            result = expected.equals(fingerprint);
        }

        if (actual != null) {
            actual.setLength(0);
            actual.append(fingerprint);
        }

        return result;
    }

    public static final String getKeyFingerprint(final PublicKey key, String digest) {
        if (digest == null) {
            digest = "SHA-256";
        }

        try {
            MessageDigest md = MessageDigest.getInstance(digest);
            md.update(getKeyBytes(key));

            String fingerprint;
            if (MD5.equals(digest)) {
                StringBuilder s = new StringBuilder();
                s.append(MD5);
                for (byte b : md.digest()) {
                    s.append(':');
                    s.append(String.format("%02x", b));
                }
                fingerprint = s.toString();
            } else {
                fingerprint = String.format(
                    "%s:%s",
                    digest.toUpperCase().replace("-", ""),
                    new Base64(0).encodeToString(md.digest()).replaceAll("=", "")
                );
            }

            if (log.isDebugEnabled()) {
                log.debug("Fingerprint: {}", fingerprint);
            }

            return fingerprint;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String getKeyFingerprint(final PublicKey key) throws Exception {
        return getKeyFingerprint(key, null);
    }

    /*
     * commons-codec <= 1.4 has Base64.isArrayByteBase64, but it is deprecated;
     * commons-codec >= 1.5 has Base64.isBase64 which works on byte[], but
     * it treats whitespace as valid. So, we roll out our own version.
     */
    private static boolean isBase64(byte[] octects) {
        for (int i = 0; i < octects.length; i++) {
            if (!Base64.isBase64(octects[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean isPublicKeyValid(String publicKey) {
        int i = publicKey.indexOf("\n");
        if (i != -1 && i != publicKey.length()-1) {
            return false;
        }

        /*
         * An OpenSSH public key consists of:
         * [mandatory] The key type
         * [mandatory] A chunk of PEM-encoded data (PEM is a specific type of Base64 encoding)
         * [optional] A comment
         */
        String[] words = publicKey.split("\\s+", 3);

        if (words.length < 2) {
            return false;
        }

        /*
         * As per http://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
         * these character class are US-ASCII only.
         */
        if (!words[0].matches("^[\\p{Alpha}\\p{Digit}-]*$")) {
            return false;
        }

        if (!isBase64(words[1].getBytes(StandardCharsets.UTF_8))) {
            return false;
        }

        return true;
    }

    public static boolean arePublicKeysValid(String publicKeys) {
        for (String publicKey : publicKeys.split("\n")) {
            if (!isPublicKeyValid(publicKey)) {
                return false;
            }
        }
        return true;
    }
}
