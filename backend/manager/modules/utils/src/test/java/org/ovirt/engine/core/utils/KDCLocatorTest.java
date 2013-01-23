package org.ovirt.engine.core.utils;

import junit.framework.Assert;

import org.junit.Test;

import org.ovirt.engine.core.utils.dns.DnsSRVLocator.DnsSRVResult;
import org.ovirt.engine.core.utils.kerberos.KDCLocator;

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
        String[] entries = result.getAddresses();
        for (String entry : entries) {

            System.out.println(entry);
        }
    }
}
