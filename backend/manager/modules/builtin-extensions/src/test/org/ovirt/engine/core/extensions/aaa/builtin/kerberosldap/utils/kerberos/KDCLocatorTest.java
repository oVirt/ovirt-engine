package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos;

import org.junit.Assert;
import org.junit.Test;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.dns.DnsSRVLocator.DnsSRVResult;

public class KDCLocatorTest {

    @Test
    public void testKDCLocator() {
        KDCLocator locator = new KDCLocator();
        String[] recordsList =
                { "0 100 88 example.com.", "0 100 88 comp1.example.com",
                        "1 100 88 comp1.example.com." };
        DnsSRVResult result = locator.getKdc(recordsList, "example.com");
        int numOfEntries = result.getNumOfValidAddresses();
        Assert.assertEquals(numOfEntries, 3);
    }
}
