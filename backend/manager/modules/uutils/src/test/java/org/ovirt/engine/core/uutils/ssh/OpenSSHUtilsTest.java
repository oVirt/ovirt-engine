package org.ovirt.engine.core.uutils.ssh;

import static org.junit.Assert.assertEquals;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

public class OpenSSHUtilsTest {

    private static PublicKey decodeKey(final String encoding) throws Exception {
        final byte[] bytes = Base64.decodeBase64(encoding);
        final KeyFactory factory = KeyFactory.getInstance("RSA");
        final X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        return factory.generatePublic(spec);
    }

    private static void testFingerprintString(final String keyEncoding, final String goodFingerprintString, String algo) throws Exception {
        final PublicKey key = decodeKey(keyEncoding);
        final String fingerprintString = OpenSSHUtils.getKeyFingerprint(key, algo);
        assertEquals(goodFingerprintString, fingerprintString);
    }

    @Test
    public void testFingerprintStrings() throws Exception {
        for (String[] key : KEYS) {
            testFingerprintString(key[0], key[1], "MD5");
            testFingerprintString(key[0], key[2], "SHA-256");
        }
    }

    private static void testKeyString(final String keyEncoding, final String goodKeyString) throws Exception {
        final PublicKey key = decodeKey(keyEncoding);
        final String keyString = OpenSSHUtils.getKeyString(key, null);
        assertEquals(goodKeyString, keyString);
    }

    @Test
    public void testKeyStrings() throws Exception {
        for (String[] key : KEYS) {
            testKeyString(key[0], key[3]);
        }
    }

    /**
     * This test data has been generated as follows:
     *
     * <pre>
     * # Generate a self signed certificate:
     * # Generate a dummy self signed certificate:
     * keytool \
     * -keystore test.jks \
     * -genkey \
     * -alias test \
     * -keyalg rsa \
     * -keysize 1024 \
     * -keypass test \
     * -storepass test \
     * -dname cn=test
     *
     * # Export the certificate:
     * keytool \
     * -keystore test.jks \
     * -rfc \
     * -export \
     * -storepass password \
     * -alias test \
     * -file test.pem
     *
     * # Extract the public key:
     * openssl x509 -noout -in test.pem -pubkey > test.tmp
     *
     * # Convert the public key to ssh format:
     * ssh-keygen -i -m pkcs8 -f test.tmp > test.ssh
     *
     * # Generate the fingerprint:
     * ssh-keygen -f test.ssh -l > test.fp
     * </pre>
     */
    private static final String[][] KEYS = {
        {
            // Key:
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCa3+YrPFsT7orhMQx0VIs+xqj/" +
            "zcVBN6zLv2lzIZIW/sDQ+sKmZKIT4wWj0GIW8ShD2dW3QNS/18GSq/MXeaih9kUJ" +
            "2Mx+DapWIHxEhDR2fBAlQgxB9/+XyNzSxWwrrNFox7tlvNmCEqN5HxdBR5fxqw4O" +
            "ODNh3JfzLcVNzqDsqwIDAQAB",

            // Fingerprint MD5
            "16:e1:9e:89:1e:ed:cc:3d:d8:af:d1:83:6e:b0:da:ae",

            // Fingerprint SHA256
            "SHA256:I4ud9yJLWcxsanCu0bXL6SxjTxj9/wbPi4JqtR1ophw",

            // SSH:
            "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQCa3+YrPFsT7orhMQx0VIs+xqj/zcVBN6zLv2lzIZIW/sDQ+sKmZKIT4wWj0GIW8ShD2dW3QNS/18GSq/MXeaih9kUJ2Mx+DapWIHxEhDR2fBAlQgxB9/+XyNzSxWwrrNFox7tlvNmCEqN5HxdBR5fxqw4OODNh3JfzLcVNzqDsqw==\n",
        },
        {
            // Key:
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCP5HriASnFNB0PqE9M/QLMR2kB" +
            "bcD7v/4yVsvosyIHw4cpRdfWTsqZAYgMHnwSsvDWTPhx6XCmyx/41pjR4H0bLINh" +
            "qjbpQUjSwiDrXTLUiU4MJcDAFsFWabbj3cZksVSTuqxR6ljdXMLJd8lrJXz1mLi1" +
            "gYKAEfF6MbwzZwcwhwIDAQAB",

            // Fingerprint MD5
            "a2:55:07:d3:b6:69:7c:ca:8f:33:e7:22:f2:12:48:d9",

            // Fingerprint SHA256
            "SHA256:5xWt5k1RhhX+EHqhzLxlqEW50QxiBIN2ng78SFbf2Rk",

            // SSH:
            "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQCP5HriASnFNB0PqE9M/QLMR2kBbcD7v/4yVsvosyIHw4cpRdfWTsqZAYgMHnwSsvDWTPhx6XCmyx/41pjR4H0bLINhqjbpQUjSwiDrXTLUiU4MJcDAFsFWabbj3cZksVSTuqxR6ljdXMLJd8lrJXz1mLi1gYKAEfF6MbwzZwcwhw==\n",
        },
        {
            // Key:
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC5AkL8nfbs0ANq1MGQL9WYISuQ" +
            "8NtYMZ7MiH/7af0Mvy5K1nDUysqZt0BFP2Yd9/bScyxdgSp7jux//i2UVINrVFCn" +
            "B5JOzCzO79d3W5glGavSiUqaqOXBLfCFNcNhmJvKVhFAXyJ4JM3v2e8Dg/PtBT73" +
            "5+YCSkrnZOSr+if8GwIDAQAB",

            // Fingerprint MD5
            "0e:82:e9:96:e7:b1:35:2b:c0:14:49:09:1c:8d:80:ee",

            // Fingerprint SHA256
            "SHA256:KLeBSl0NAL6T1Z8EEqsvpW1c3y9mtVGmogUNxIuL9is",

            // SSH:
            "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQC5AkL8nfbs0ANq1MGQL9WYISuQ8NtYMZ7MiH/7af0Mvy5K1nDUysqZt0BFP2Yd9/bScyxdgSp7jux//i2UVINrVFCnB5JOzCzO79d3W5glGavSiUqaqOXBLfCFNcNhmJvKVhFAXyJ4JM3v2e8Dg/PtBT735+YCSkrnZOSr+if8Gw==\n",
        },
        {
            // Key:
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCRYaSHFlFePROfmsj82O6KMCW3" +
            "wMlF93ifYXA8T1b87AybzORNhR49uCi9jccEpEcRzOmC9lpHYt3iouGCtFfrBWgR" +
            "Pv1i4Iln5gqJFbaT9/48Zli3AraKbVWBJQeDKQL0EywU0sXz2upN0OMwyehQAZsa" +
            "9KWvwt2LV2c8cbMprwIDAQAB",

            // Fingerprint MD5
            "b0:d1:fc:40:95:39:25:20:c4:3f:7b:b8:6f:18:4d:ae",

            // Fingerprint SHA256
            "SHA256:hDXnC4yi0dCY8uqtecpi7x3qPk4hwetfZigGpwoMrCo",

            // SSH:
            "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQCRYaSHFlFePROfmsj82O6KMCW3wMlF93ifYXA8T1b87AybzORNhR49uCi9jccEpEcRzOmC9lpHYt3iouGCtFfrBWgRPv1i4Iln5gqJFbaT9/48Zli3AraKbVWBJQeDKQL0EywU0sXz2upN0OMwyehQAZsa9KWvwt2LV2c8cbMprw==\n",
        },
        {
            // Key:
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDMJJqXjfcpxpb3VPUXvWl+4R2K" +
            "qf5piN3YNwIbBQiZobSnvoY7oMPVFBh0AW1DJa3VD0JOSRqcYn+zbo3/mEln7+/Z" +
            "DXYWsZY1/UgL9QNl0PrrugWKzQfU1lB7T+TFJQNeYH4xUtORGdnfO+uMTK9h2yC/" +
            "uCMvqv3dLKR1SGRzEQIDAQAB",

            // Fingerprint MD5
            "56:c1:e3:c5:bc:75:21:00:67:65:c2:06:39:82:bf:9f",

            // Fingerprint SHA256
            "SHA256:tO9ZbfmciAOQYdr/yk5+EGeEW51jBmMM/bDhc2Y47x8",

            // SSH:
            "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQDMJJqXjfcpxpb3VPUXvWl+4R2Kqf5piN3YNwIbBQiZobSnvoY7oMPVFBh0AW1DJa3VD0JOSRqcYn+zbo3/mEln7+/ZDXYWsZY1/UgL9QNl0PrrugWKzQfU1lB7T+TFJQNeYH4xUtORGdnfO+uMTK9h2yC/uCMvqv3dLKR1SGRzEQ==\n",
        },
        {
            // Key:
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDHlrWc38JjFHNvZ7gs6MOlvrFL" +
            "F2K+U8CHkqV+7/WFCszZGeJPo9va9uIjdVD7RHYKOiOt2r6xulO/RRjxY4h8coU9" +
            "rNVUmaCRUcsqclJpVURFrLUbdIn4fGMVnxeFrI+cKW34A9JFu4cGbZ2s7eOQNBJT" +
            "LKktR9T/nxc8D9H+/wIDAQAB",

            // Fingerprint MD5
            "55:d5:de:fc:17:d4:b1:06:22:73:67:f5:e0:08:bf:25",

            // Fingerprint SHA256
            "SHA256:GtqEmRC7wauTPZcuI14NeyIq/OBRIZfO8A5IoFyCWl8",

            // SSH:
            "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQDHlrWc38JjFHNvZ7gs6MOlvrFLF2K+U8CHkqV+7/WFCszZGeJPo9va9uIjdVD7RHYKOiOt2r6xulO/RRjxY4h8coU9rNVUmaCRUcsqclJpVURFrLUbdIn4fGMVnxeFrI+cKW34A9JFu4cGbZ2s7eOQNBJTLKktR9T/nxc8D9H+/w==\n",
        },
        {
            // Key:
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCYDKTQXJWxY5GNtQyz40osYjui" +
            "aqP5B+C0xN8lteW/zrQ9rwyZ+ijm15uY0vH4gggpn5oYkIbu/5mrk7cNan5Ygs5M" +
            "VP7QfQ6OSV1c8alckmAA4aFjq84O3dRIM4Vj97FwiENuzcLsBSlPxU4WFhKNrLEL" +
            "NNuIVKPltQVnJUA0AwIDAQAB",

            // Fingerprint MD5
            "23:90:4e:18:11:fc:44:f8:4a:3e:5b:f3:a7:3c:cb:14",

            // Fingerprint SHA256
            "SHA256:FmRM9fOZfn3yAkik/vE799kR0msftllHsxWh4WK+WDU",

            // SSH:
            "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQCYDKTQXJWxY5GNtQyz40osYjuiaqP5B+C0xN8lteW/zrQ9rwyZ+ijm15uY0vH4gggpn5oYkIbu/5mrk7cNan5Ygs5MVP7QfQ6OSV1c8alckmAA4aFjq84O3dRIM4Vj97FwiENuzcLsBSlPxU4WFhKNrLELNNuIVKPltQVnJUA0Aw==\n",
        },
        {
            // Key:
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCbfXqtv9/jI022H5t4T9qI3oBB" +
            "aLFBSLx2J/MP4XQ32L5/arQIyiu25mPNwcnwo7h1teCVr722TS2m8Sg9TXWwnd13" +
            "l5VwwKqOrlOVhi3lj6ljpWBI/viBMMJNOeG1K4x8ZO1x6L3h8UtmIU599VqjpaPg" +
            "wQNFlOweUqh4h0sRtwIDAQAB",

            // Fingerprint MD5
            "61:a8:d6:8d:ca:27:86:50:ad:5f:de:1e:a6:17:c0:42",

            // Fingerprint SHA256
            "SHA256:N0jkalb2Z9nK/tjmYlfECOiTSOVP/OSRHmHmj1BOePk",

            // SSH:
            "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQCbfXqtv9/jI022H5t4T9qI3oBBaLFBSLx2J/MP4XQ32L5/arQIyiu25mPNwcnwo7h1teCVr722TS2m8Sg9TXWwnd13l5VwwKqOrlOVhi3lj6ljpWBI/viBMMJNOeG1K4x8ZO1x6L3h8UtmIU599VqjpaPgwQNFlOweUqh4h0sRtw==\n",
        },
        {
            // Key:
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDJmCyxIsgvYvhymxcqZF0eX3bL" +
            "/IA26Ygr4hZ+Q4NidYXZZ3cYOJvgdj8zoJu/+I3jW2re0Kltj+BqHssWD1WIO2rX" +
            "0/1UZP95KRlCLfa8Nnqi889NNpUhvHJnfBqzbyBbgDFDMZoi2NVEx9nUpZRr8e6D" +
            "1rA4kS4jQURTLODjrwIDAQAB",

            // Fingerprint MD5
            "37:49:1d:28:20:98:1a:da:e7:29:b3:96:61:2b:f1:40",

            // Fingerprint SHA256
            "SHA256:OQfj4/W7qD5rfqy81+2gUiX+cPdlVGWupfxZ5Z6GGIM",

            // SSH:
            "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQDJmCyxIsgvYvhymxcqZF0eX3bL/IA26Ygr4hZ+Q4NidYXZZ3cYOJvgdj8zoJu/+I3jW2re0Kltj+BqHssWD1WIO2rX0/1UZP95KRlCLfa8Nnqi889NNpUhvHJnfBqzbyBbgDFDMZoi2NVEx9nUpZRr8e6D1rA4kS4jQURTLODjrw==\n",
        },
        {
            // Key:
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC3Cz4oruqQv9fz+NOZnhvGugWv" +
            "Ppuwh44aGVdYm0iXJZCq76bgw0ajDF6XhVs5xYagWEO31vVKVu7lTMIv7OcoAw3V" +
            "C3giBDJotkkXO7uR3iAQAGZrARxRrOOhUNqVKIuslw/+YcvgsQl5TdgflvrdH2zQ" +
            "yVm2/0qLjdCN8lYahwIDAQAB",

            // Fingerprint MD5
            "6d:cd:bc:99:c0:83:ca:b1:8e:58:10:c3:b8:4d:56:ee",

            // Fingerprint SHA256
            "SHA256:pbpMFSx/C7wm34+zg3ky8ALQytzn1QzDOYE1ohHWGWw",

            // SSH:
            "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQC3Cz4oruqQv9fz+NOZnhvGugWvPpuwh44aGVdYm0iXJZCq76bgw0ajDF6XhVs5xYagWEO31vVKVu7lTMIv7OcoAw3VC3giBDJotkkXO7uR3iAQAGZrARxRrOOhUNqVKIuslw/+YcvgsQl5TdgflvrdH2zQyVm2/0qLjdCN8lYahw==\n",
        },
    };

}
