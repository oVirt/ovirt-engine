package org.ovirt.engine.core.uutils.crypto;

import static org.junit.Assert.assertArrayEquals;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.KeyException;
import java.security.KeyStore;

import org.junit.Test;

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

        byte[] test = "testing 1 2 3 4".getBytes(Charset.forName("UTF-8"));

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

        byte[] test = "testing 1 2 3 4".getBytes(Charset.forName("UTF-8"));
        String blob = "eyJjaXBoZXJBbGdvIjoiQUVTL09GQi9QS0NTNVBhZGRpbmciLCJpdiI6Im01aDgxYUxmdkNycXZtS3R3L290SFE9PSIsIndyYXBLZXlEaWdlc3QiOiJXNkdCbHFrRUlwbUx2aE94VFlybHpwV1JrQjg9Iiwid3JhcHBlZEtleSI6ImE1TXhCWFFBY25kZi95U2J4YWRMdWwySWxHNFpkTWgzOHlUdE9HLzI2MDBHR204ZHRudklpSSsxakE3c0k4cDI1VVAwbWFBL29KdG9xTUVHd0Fhc3VNdkxhVEFPbHJId1RwM2ROaWlWSUFlNmREY21WVTYrd0JLNEljMjBNdjluKzQrRVExUTZ6OXBWSldXQjEzNXk4TEM3NlUzK2czVnBkVm5SWFNGTllpUjR3L2xGMFllVVFxR1J1RnlVRzJLUEV1RzFZTjl5amZjeDB1RWxKb1VYRGNoUDlOY3VqaE9pUHcvTUJ0YzRzSjNjTVZobFh1ZXFtTG85ZE1KWngzS2wzWkNFQTlrMS9Md2wvRzZwNXlsc2gxeWwxSmhwL2M3RmRDRjRJcWt6cFZVejZsZXd4cktnZFhlSG1oOE1CWk5BdkJIK2plYmZrRitlcHZ0c0t0SWJBZz09IiwiZW5jcnlwdGVkQ29udGVudCI6IlhJajluaTFQL1VVZUptOUhYc0orYWIvSkxGS1p1ZnRlM0c1TkFIME0xbkFUR3VtMEwweEhNKzhSYUY0aHZ6dFJ1S3FZb0cxYytmR3dVTG9rRW1pejczZzRmQzZhUTBWL3BManNray81eW43OEZQYVpOQU9oM1ByNmFOemNGbkR0NVBnOVJiK3h5TzIzM0ZlTGw4bWMvOFVRSUEzUURQT1pvbmlxWVdEOXM5VEhYbG56Z1dUSE5GY2g3U0VSZHZqOUtRdHcvUkxPMS9sTVMybmtONUJUZHozYkx2ZXY5YjNWMWdrY1ZKRDRpams9Iiwid3JhcEtleURpZ2VzdEFsZ28iOiJTSEEtMSIsIndyYXBBbGdvIjoiUlNBL0VDQi9QS0NTMVBhZGRpbmcifQ==";

        assertArrayEquals(
            test,
            EnvelopeEncryptDecrypt.decrypt(
                entry,
                blob
            )
        );
    }

    @Test(expected=KeyException.class)
    public void testInvalidKey() throws Exception {
        KeyStore.PrivateKeyEntry entry1 = getPrivateKeyEntry(getKeyStore("PKCS12", "key.p12", "NoSoup4U"), "1", "NoSoup4U");
        KeyStore.PrivateKeyEntry entry2 = getPrivateKeyEntry(getKeyStore("PKCS12", "key2.p12", "mypass"), "1", "mypass");

        byte[] test = "testing 1 2 3 4".getBytes(Charset.forName("UTF-8"));

        assertArrayEquals(
            test,
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
