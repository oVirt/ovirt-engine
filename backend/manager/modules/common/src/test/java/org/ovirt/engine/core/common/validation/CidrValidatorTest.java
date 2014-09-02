package org.ovirt.engine.core.common.validation;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CidrValidatorTest {
    private final String cidr;
    private final boolean validCidrFormatExpectedResult;
    private final boolean validNetworkAddressExpectedResult;

    public CidrValidatorTest(String cidr,
            boolean validCidrFormatExpectedResult,
            boolean validNetworkAddressExpectedResult) {
        this.cidr = cidr;
        this.validCidrFormatExpectedResult = validCidrFormatExpectedResult;
        this.validNetworkAddressExpectedResult = validNetworkAddressExpectedResult;
    }

    @Test
    public void checkCidrFormatValidation() {
        assertEquals("Failed to validate CIDR's Format: " + cidr,
                validCidrFormatExpectedResult,
                CidrValidator.getInstance().isCidrFormatValid(cidr));
    }

    @Test
    public void checkNetworkAddressValidation() {
        if (!validCidrFormatExpectedResult) {
            return;
        }

        assertEquals("Failed to validate CIDR's network address" + cidr,
                validNetworkAddressExpectedResult,
                CidrValidator.getInstance().isCidrNetworkAddressValid(cidr));
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // Bad Format
                { null, false, false },
                { "", false, false },
                { "?\"?>!", false, false },
                { "a", false, false },
                { "a.a", false, false },
                { "a.a.a", false, false },
                { "a.a.a.a", false, false },
                { "a.a.a.a/a", false, false },
                { "1", false, false },
                { "1.1.1.1", false, false },
                { "1.1.1.1/", false, false },
                { "1000.1.1.1/24", false, false },
                { "1.1000.1.1/24", false, false },
                { "1.1.1000.1/24", false, false },
                { "1.1.1.1000/24", false, false },

                { "1.1.1.1/33", false, false },
                { "1111.1.1.1/1", false, false },
                { "1111.1.1.1/32", false, false },
                { "1.1.1.1/1/1", false, false },
                { "1.1/1.1/.1/.1", false, false },
                { "256.1.1.1/1", false, false },
                { "256.1.1.1/32", false, false },
                { "256.1.1.1/222222222222222222222222", false, false },
                { "255.?.?././", false, false },
                { "255?23?1?0/8", false, false },
                { "23\22\22\22\22\\", false, false },
                { ".................", false, false },
                { "././././", false, false },
                { "?/?/?/?/", false, false },

                // Not A Network address
                { "253.0.0.32/26", true, false },
                { "255.255.255.192/25", true, false },
                { "255.255.255.16/24", true, false },
                { "255.254.192.0/17", true, false },
                { "255.255.255.250/16", true, false },
                { "255.255.192.17/14", true, false },
                { "255.128.0.0/8", true, false },
                { "240.255.255.247/3", true, false },
                { "224.0.0.0/2", true, false },
                { "192.0.0.0/1", true, false },
                { "255.255.255.255/0", true, false },

                // valid CIDR
                { "255.255.255.255/32", true, true },
                { "0.0.0.0/32", true, true },
                { "255.255.255.254/31", true, true },
                { "255.255.255.248/29", true, true },
                { "255.255.255.0/24", true, true },
                { "255.255.254.0/23", true, true },
                { "255.0.254.0/23", true, true },
                { "255.255.0.0/16", true, true },
                { "255.252.0.0/14", true, true },
                { "255.0.0.0/8", true, true },
                { "248.0.0.0/5", true, true },
                { "128.0.0.0/1", true, true },
        });
    }

}
