package org.ovirt.engine.core.uutils.crypto;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyException;
import java.security.KeyStore;

import org.junit.jupiter.api.Test;

public class EnvelopeEncryptDecryptTest {

    private static KeyStore getKeyStore(String storeType, String store, String password) throws Exception {
        KeyStore ks = KeyStore.getInstance(storeType);
        try (InputStream is = ClassLoader.getSystemResourceAsStream(store)) {
            ks.load(is, password.toCharArray());
        }
        return ks;
    }

    private static KeyStore.PrivateKeyEntry getPrivateKeyEntry(KeyStore ks, String alias, String password) throws Exception {
        return (KeyStore.PrivateKeyEntry)ks.getEntry(alias, new KeyStore.PasswordProtection(password.toCharArray()));
    }

    @Test
    public void test1() throws Exception {
        KeyStore.PrivateKeyEntry entry = getPrivateKeyEntry(getKeyStore("PKCS12", "key.p12", "NoSoup4U"), "1", "NoSoup4U");

        byte[] test = "testing 1 2 3 4".getBytes(StandardCharsets.UTF_8);

        assertArrayEquals(
            test,
            EnvelopeEncryptDecrypt.decrypt(
                entry,
                EnvelopeEncryptDecrypt.encrypt(
                    "AES/OFB/PKCS5Padding",
                    256,
                    entry.getCertificate(),
                    100,
                    test
                )
            )
        );
    }

    @Test
    public void test2() throws Exception {
        KeyStore.PrivateKeyEntry entry = getPrivateKeyEntry(getKeyStore("PKCS12", "key.p12", "NoSoup4U"), "1", "NoSoup4U");

        byte[] test = "testing 1 2 3 4".getBytes(StandardCharsets.UTF_8);
        String blob = "eyJhcnRpZmFjdCI6IkVudmVsb3BlRW5jcnlwdERlY3J5cHQiLCJjaXBoZXJBbGdvIjoiQUVTL09GQi9QS0NTNVBhZGRpbmciLCJpdiI6Ilg0T05TVXg5NWRvTWdETXo5cjRDUmc9PSIsIndyYXBLZXlEaWdlc3QiOiJXNkdCbHFrRUlwbUx2aE94VFlybHpwV1JrQjg9Iiwid3JhcHBlZEtleSI6InFCRkZqU2t2K2szbnQxZFlUdS93LzFCTDdZbWIrMllZZ2JFYVFQVVhESDlVN0hmcHpXV2xLTGNyZHZQNzdFQWZpQWJqS0lIS0cydW1xR29xelNMM29qRTNFU1hESEZIYjc4bncwWWhTZkx3UFN5amxtTGtJQVU0N2x2S0dGVDFicmpKemQ1SU1FdnA2dytmZklvNUticWwwTmpVb2l0b1BGVTFKeHlIVVBrajBHRVV0MGNEaENuNU44bnd2K1VxS3NKZ00wY29PMy9hN25zZGxtWHprU0I2dDZhUFllRyt6YUx2UnVnSWFGTElueDExSkhlK3QvUDdZNXlJWkZyMkFIMVZNeFcyRy9oK1daczB1Z3BXRHlZWmpuLzh2aFRJN05CMmdqMUJQci9PS3VJNkFQWHcySmQ5eStwc3dhNlZtV1FVK3ZJeVNQWjV0YWcycXBtYjZ0Zz09IiwiZW5jcnlwdGVkQ29udGVudCI6IjVXWDdtamcyeHZTdzIzNzFGY2RYNENnSDIzdytLdVhjN1BMU2tnUVN1RzFyNWhMZXJjVldqc1NlTU1lUjV3aU5TWnM3Nmt0dTQ3V2NzVFFtVmFLcHl0amwvcWhseS84dDRlejROVXYyeStQYS9maWNEZFFGQmZzVDRCRjlYd3JlNlhSODl3ajFBNFRmL2Z4bDUvcGlFUCs3Sjh1R3JCUStMdzZFZDNyNDM5Y1ZOcDBVRVNDRmtVMCtEZzNHVis1WmRueHJET1hqTnRRUDlvdHg3aE5qcWhYNzk1TmFIdjZZTE9EdmxHemhSbXM9Iiwid3JhcEtleURpZ2VzdEFsZ28iOiJTSEEtMSIsIndyYXBBbGdvIjoiUlNBL0VDQi9QS0NTMVBhZGRpbmciLCJ2ZXJzaW9uIjoiMSJ9";

        assertArrayEquals(
            test,
            EnvelopeEncryptDecrypt.decrypt(
                entry,
                blob
            )
        );
    }

    @Test
    public void testInvalidKey() throws Exception {
        KeyStore.PrivateKeyEntry entry1 = getPrivateKeyEntry(getKeyStore("PKCS12", "key.p12", "NoSoup4U"), "1", "NoSoup4U");
        KeyStore.PrivateKeyEntry entry2 = getPrivateKeyEntry(getKeyStore("PKCS12", "key2.p12", "mypass"), "1", "mypass");

        byte[] test = "testing 1 2 3 4".getBytes(StandardCharsets.UTF_8);

        assertThrows(KeyException.class, () ->
                EnvelopeEncryptDecrypt.decrypt(
                    entry2,
                    EnvelopeEncryptDecrypt.encrypt(
                        "AES/OFB/PKCS5Padding",
                        256,
                        entry1.getCertificate(),
                        100,
                        test
                    )
                )
        );
    }

}
