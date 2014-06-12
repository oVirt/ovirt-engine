package org.ovirt.engine.core.uutils.crypto;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * glibc's crypt using md5.
 * openssl passwd -1
 * until a newer commons-codec is bundled with jboss.
 */
public class CryptMD5 {

    private final static char[] b64t = {
        '.', '/',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    };

    private final static String SALT_PREFIX = "$1$";
    private final static String SALT_DELIMITER = "$";
    private final static int SALT_MAX_LENGTH = 8;

    protected static String encode(byte b1, byte b2, byte b3, int n) {
        StringBuilder result = new StringBuilder();
        int w = ((b1&0xff) << 16) | ((b2&0xff) << 8) | (b3&0xff);
        while (n-- > 0) {
            result.append(b64t[w&0x3f]);
            w = w >> 6;
        }
        return result.toString();
    }

    public static String crypt(String password, String salt) {
        try {
            byte[] passwordBytes = password.getBytes(Charset.forName("UTF-8"));
            byte[] saltBytes = salt.getBytes(Charset.forName("UTF-8"));
            byte[] altresult;

            if (salt.length() > SALT_MAX_LENGTH) {
                salt = salt.substring(0, SALT_MAX_LENGTH);
            }

            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(passwordBytes);
            digest.update(SALT_PREFIX.getBytes(Charset.forName("UTF-8")));
            digest.update(saltBytes);

            MessageDigest altdigest = MessageDigest.getInstance("MD5");
            altdigest.update(passwordBytes);
            altdigest.update(saltBytes);
            altdigest.update(passwordBytes);
            altresult = altdigest.digest();

            {
                int i = passwordBytes.length;
                while (i > 16) {
                    digest.update(altresult, 0, 16);
                    i -= 16;
                }
                digest.update(altresult, 0, i);
            }

            altresult[0] = '\000';
            for (int i = passwordBytes.length; i > 0; i >>= 1) {
                digest.update((i & 1) != 0 ? altresult : passwordBytes, 0, 1);
            }
            altresult = digest.digest();

            for (int i = 0; i < 1000; ++i) {
                digest.reset();
                if ((i & 1) != 0) {
                    digest.update(passwordBytes);
                } else {
                    digest.update(altresult, 0, 16);
                }
                if ((i % 3) != 0) {
                    digest.update(saltBytes);
                }
                if ((i % 7) != 0) {
                    digest.update(passwordBytes);
                }
                if ((i & 1) != 0) {
                    digest.update(altresult, 0, 16);
                } else {
                    digest.update(passwordBytes);
                }
                altresult = digest.digest();
            }

            StringBuilder builder = new StringBuilder().append(
                SALT_PREFIX
            ).append(
                salt
            ).append(
                SALT_DELIMITER
            ).append(
                encode(altresult[0], altresult[6], altresult[12], 4)
            ).append(
                encode(altresult[1], altresult[7], altresult[13], 4)
            ).append(
                encode(altresult[2], altresult[8], altresult[14], 4)
            ).append(
                encode(altresult[3], altresult[9], altresult[15], 4)
            ).append(
                encode(altresult[4], altresult[10], altresult[5], 4)
            ).append(
                encode((byte) 0, (byte) 0, altresult[11], 2)
            );
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String crypt(String password) {
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            byte[] r = new byte[SALT_MAX_LENGTH];
            char[] salt = new char[r.length];
            random.nextBytes(r);
            for (int i=0;i<r.length;i++) {
                salt[i] = b64t[(r[i]&0xff) % b64t.length];
            }
            return crypt(password, new String(salt));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
