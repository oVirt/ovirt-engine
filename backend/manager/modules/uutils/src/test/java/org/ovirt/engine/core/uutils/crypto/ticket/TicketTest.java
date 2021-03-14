package org.ovirt.engine.core.uutils.crypto.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class TicketTest {

    private static KeyStore getKeyStore(String storeType, String store, String password) throws Exception {
        KeyStore ks = KeyStore.getInstance(storeType);
        try (InputStream is = ClassLoader.getSystemResourceAsStream(store)) {
            ks.load(is, password.toCharArray());
        }
        return ks;
    }

    private static KeyStore.PrivateKeyEntry getPrivateKeyEntry(KeyStore ks, String alias, String password)
            throws Exception {
        return (KeyStore.PrivateKeyEntry) ks.getEntry(alias, new KeyStore.PasswordProtection(password.toCharArray()));
    }

    /*
     * @Test public void testByCertificate() throws Exception { final String content = "testByCertificate";
     * KeyStore.PrivateKeyEntry entry = getPrivateKeyEntry(getKeyStore("PKCS12", "ticket/ca1test1.p12", "test1"), "1",
     * "test1"); String ticket = new TicketEncoder(entry.getCertificate(), entry.getPrivateKey(), 30).encode(content);
     * assertEquals(content, new TicketDecoder(entry.getCertificate()).decode(ticket)); assertEquals( content, new
     * TicketDecoder( getKeyStore("JKS", "ticket/ca1.jks", "changeit"), null, entry.getCertificate() ).decode(ticket) );
     * }
     */

    @Test
    public void testByCertificateFailCertificate() throws Exception {
        final String content = "testByCertificate";
        KeyStore.PrivateKeyEntry entry1 =
                getPrivateKeyEntry(getKeyStore("PKCS12", "ticket/ca1test1.p12", "test1"), "1", "test1");
        KeyStore.PrivateKeyEntry entry2 =
                getPrivateKeyEntry(getKeyStore("PKCS12", "ticket/ca1test2.p12", "test2"), "1", "test2");
        assertThrows(GeneralSecurityException.class,
                () -> new TicketDecoder(
                        entry2.getCertificate()).decode(
                                new TicketEncoder(entry1.getCertificate(), entry1.getPrivateKey()).encode(content)));
    }

    @Test
    public void testByCertificateFailCA() throws Exception {
        final String content = "testByCertificate";
        KeyStore.PrivateKeyEntry entry =
                getPrivateKeyEntry(getKeyStore("PKCS12", "ticket/ca1test1.p12", "test1"), "1", "test1");
        assertThrows(GeneralSecurityException.class,
                () -> new TicketDecoder(
                        getKeyStore("JKS", "ticket/ca2.jks", "changeit"),
                        null,
                        entry.getCertificate()).decode(
                                new TicketEncoder(entry.getCertificate(), entry.getPrivateKey()).encode(content)));
    }

    /*
     * @Test public void testByEKU() throws Exception { final String content = "testByEKU"; KeyStore.PrivateKeyEntry
     * entry = getPrivateKeyEntry(getKeyStore("PKCS12", "ticket/ca1test2.p12", "test2"), "1", "test2"); assertEquals(
     * content, new TicketDecoder( getKeyStore("JKS", "ticket/ca1.jks", "changeit"), "1.2.3.4" ).decode( new
     * TicketEncoder(entry.getCertificate(), entry.getPrivateKey()).encode(content) ) ); }
     */
    @Test
    public void testByEKUFailEKU() throws Exception {
        final String content = "testByEKU";
        KeyStore.PrivateKeyEntry entry =
                getPrivateKeyEntry(getKeyStore("PKCS12", "ticket/ca1test2.p12", "test2"), "1", "test2");
        assertThrows(GeneralSecurityException.class,
                () -> new TicketDecoder(
                        getKeyStore("JKS", "ticket/ca1.jks", "changeit"),
                        "1.2.3.4.5").decode(
                                new TicketEncoder(entry.getCertificate(), entry.getPrivateKey()).encode(content)));
    }

    @Test
    public void testByEKUFailCA() throws Exception {
        final String content = "testByEKU";
        KeyStore.PrivateKeyEntry entry =
                getPrivateKeyEntry(getKeyStore("PKCS12", "ticket/ca1test2.p12", "test2"), "1", "test2");
        assertThrows(GeneralSecurityException.class,
                () -> new TicketDecoder(
                        getKeyStore("JKS", "ticket/ca2.jks", "changeit"),
                        "1.2.3.4").decode(
                                new TicketEncoder(entry.getCertificate(), entry.getPrivateKey()).encode(content)));
    }

    @Test
    public void testSalt() throws Exception {
        final String content = "testSalt";
        KeyStore.PrivateKeyEntry entry =
                getPrivateKeyEntry(getKeyStore("PKCS12", "ticket/ca1test2.p12", "test2"), "1", "test2");

        Set<String> salt = new HashSet<>();
        final int n = 10;
        for (int i = 0; i < n; i++) {
            salt.add(
                    new ObjectMapper().<Map<String, String>> readValue(
                            Base64.decodeBase64(
                                    new TicketEncoder(entry.getCertificate(), entry.getPrivateKey()).encode(content)),
                            TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class))
                            .get("salt"));
        }
        assertEquals(n, salt.size());
    }

    @Test
    public void testContentFail() throws Exception {
        KeyStore.PrivateKeyEntry entry =
                getPrivateKeyEntry(getKeyStore("PKCS12", "ticket/ca1test2.p12", "test2"), "1", "test2");

        Map<String, String> map = new ObjectMapper().readValue(
                Base64.decodeBase64(new TicketEncoder(entry.getCertificate(), entry.getPrivateKey()).encode("content")),
                TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class));
        map.put("data", "Content");
        String modifiedTicket = new Base64(0).encodeToString(new ObjectMapper().writeValueAsString(map)
                .getBytes(
                        StandardCharsets.UTF_8));

        assertThrows(GeneralSecurityException.class,
                () -> new TicketDecoder(getKeyStore("JKS", "ticket/ca1.jks", "changeit"), "1.2.3.4")
                        .decode(modifiedTicket));
    }

    @Test
    public void testValidToFail() throws Exception {
        KeyStore.PrivateKeyEntry entry =
                getPrivateKeyEntry(getKeyStore("PKCS12", "ticket/ca1test2.p12", "test2"), "1", "test2");
        String ticket = new TicketEncoder(entry.getCertificate(), entry.getPrivateKey(), 1).encode("content");
        Thread.sleep(2000);
        assertThrows(GeneralSecurityException.class,
                () -> new TicketDecoder(getKeyStore("JKS", "ticket/ca1.jks", "changeit"), "1.2.3.4").decode(ticket));
    }
}
