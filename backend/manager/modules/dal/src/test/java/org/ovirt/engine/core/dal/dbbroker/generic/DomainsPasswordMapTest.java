package org.ovirt.engine.core.dal.dbbroker.generic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.engineencryptutils.EncryptionUtils;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.crypto*" })
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
        String keyStorePath = ClassLoader.getSystemResource(".keystore").getPath();
        String alias = "engine";
        String encryptedPassword =
                EncryptionUtils.encrypt(clearTextPassword,
                        keyStorePath,
                        keyStorePassword,
                        alias);

        DomainsPasswordMap map =
                new DomainsPasswordMap("redhat.com:" + encryptedPassword + ",jboss.com:" + encryptedPassword,
                        keyStorePath, keyStorePassword, alias);

        org.junit.Assert.assertEquals(clearTextPassword, map.get("redhat.com"));
        org.junit.Assert.assertEquals(clearTextPassword, map.get("jboss.com"));
    }



}
