package org.ovirt.engine.core.dal.dbbroker.generic;

import java.net.URLDecoder;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ovirt.engine.core.utils.crypt.EncryptionUtils;

public class DomainsPasswordMapTest {

    /**
     * Test the AdUserPassword config value which return a map type named {@link DomainsPasswordMap}; The test creates
     * an comma delimited list for each domains with the string NoSoup4U encrypted and then fetches the values decrypted
     * through the map
     * @throws Exception
     *             on encryption failure of the test password string
     */
    @Test
    public void testPasswordDomainMap() throws Exception {
        String keyStorePassword = "NoSoup4U";
        String clearTextPassword = keyStorePassword;
        String keyStorePath = URLDecoder.decode(ClassLoader.getSystemResource("key.p12").getPath(), "UTF-8");
        String alias = "1";
        String encryptedPassword =
                EncryptionUtils.encrypt(clearTextPassword,
                        keyStorePath,
                        keyStorePassword,
                        alias);

        DomainsPasswordMap map =
                new DomainsPasswordMap("redhat.com:" + encryptedPassword + ",jboss.com:" + encryptedPassword,
                        keyStorePath, keyStorePassword, alias);

        assertEquals(clearTextPassword, map.get("redhat.com"));
        assertEquals(clearTextPassword, map.get("jboss.com"));
    }

}
