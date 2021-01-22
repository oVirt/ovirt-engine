package org.ovirt.engine.core.uutils.ssh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Collections;

import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.config.keys.PublicKeyEntryResolver;
import org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyPairResourceWriter;
import org.apache.sshd.common.digest.BuiltinDigests;
import org.apache.sshd.common.util.io.SecureByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenSSHUtils {
    private static final Logger log = LoggerFactory.getLogger(OpenSSHUtils.class);

    private OpenSSHUtils() {
        // No instances allowed.
    }

    /**
     * Convert a public key to the SSH format used in the <code>authorized_keys</code> files.
     *
     * @param key
     *            the public key to convert
     * @param alias
     *            the alias to be appended at the end of the line, if it is <code>null</code> nothing will be appended
     * @return an string that can be directly written to the <code>authorized_keys</code> file or <code>null</code> if
     *         the conversion can't be performed for whatever the reason
     */
    public static String getKeyString(final PublicKey key, String alias) {
        ByteArrayOutputStream out = new SecureByteArrayOutputStream();
        try {
            OpenSSHKeyPairResourceWriter.INSTANCE.writePublicKey(key, alias, out);
            return out.toString();
        } catch (IOException | GeneralSecurityException e) {
            log.error("Could not serialized public key to SSH pub format. {}", e.getMessage());
            log.debug("Could not serialized public key to SSH pub format. More details.", e);
            return null;
        }
    }

    public static String getKeyFingerprint(final PublicKey key, String digest) {
        if (digest == null) {
            digest = "SHA-256";
        }
        return KeyUtils.getFingerPrint(BuiltinDigests.fromAlgorithm(digest), key);
    }

    @Deprecated
    public static boolean checkKeyFingerprint(String expected, final PublicKey key) {
        String digest = expected.split(":", 2)[0];
        try {
            if (digest.length() == 2) {
                Integer.parseInt(digest, 16);
                digest = "MD5";
                expected = digest + ":" + expected;
            }
        } catch (NumberFormatException e) {
            // ignore
        }

        if (!digest.startsWith("MD")) {
            digest = digest.replaceFirst("([0-9])", "-$1");
        }

        String fingerprint = getKeyFingerprint(key, digest);

        boolean result;
        if ("MD5".equals(digest)) {
            result = expected.equalsIgnoreCase(fingerprint);
        } else {
            result = expected.equals(fingerprint);
        }

        return result;
    }

    public static boolean isPublicKeyValid(String publicKey) {
        try {
            // corner case check to avoid broken comments like test@ovirt.org\nadasd\n":
            // ie. "ecdsa-sha2-nistp256 AAAAE2rninzcyBga(...) test@ovirt.org\nadasd\n"
            // left here due to backward compatibility ... if applicable this check should be done in sshd :
            // AuthorizedKeyEntry.parseAuthorizedKeyEntry
            if (publicKey != null && publicKey.split("\n").length > 1) {
                return false;
            }
            AuthorizedKeyEntry entry = AuthorizedKeyEntry.parseAuthorizedKeyEntry(publicKey);
            entry.resolvePublicKey(null, Collections.emptyMap(), PublicKeyEntryResolver.FAILING);
            return true;
        } catch (Exception e) {
            return false;
        }
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
