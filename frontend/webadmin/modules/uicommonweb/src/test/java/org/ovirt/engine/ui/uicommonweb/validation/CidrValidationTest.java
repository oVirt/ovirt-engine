package org.ovirt.engine.ui.uicommonweb.validation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Spy;

@RunWith(Parameterized.class)
public class CidrValidationTest {

    @Spy
    private CidrValidation validation;

    private final String cidr;
    private final boolean isCidrValid;

    public CidrValidationTest(String cidr, boolean isCidrValid) {
        this.cidr = cidr;
        this.isCidrValid = isCidrValid;
    }

    @Before
    public void setup() {
        validation = spy(new CidrValidation());
        doReturn(null).when(validation).getThisFieldMustContainCidrInFormatMsg();
        doReturn(null).when(validation).getCidrNotNetworkAddress();
    }

    @Test
    public void checkCidrValidation() {
        assertEquals("Failed to validate CIDR: " + cidr, isCidrValid, validation.validate(cidr).getSuccess());//$NON-NLS-1$
    }

    @Parameterized.Parameters
    public static Collection<Object[]> namesParams() {
        return Arrays.asList(new Object[][] {
                // Bad Format
                { null, false }, //$NON-NLS-1$
                { "", false }, //$NON-NLS-1$
                { "?\"?>!", false }, //$NON-NLS-1$
                { "a", false }, //$NON-NLS-1$
                { "a.a", false }, //$NON-NLS-1$
                { "a.a.a", false }, //$NON-NLS-1$
                { "a.a.a.a", false }, //$NON-NLS-1$
                { "a.a.a.a/a", false }, //$NON-NLS-1$
                { "1", false }, //$NON-NLS-1$
                { "1.1.1.1", false }, //$NON-NLS-1$
                { "1.1.1.1/", false }, //$NON-NLS-1$
                { "1000.1.1.1/24", false }, //$NON-NLS-1$
                { "1.1000.1.1/24", false }, //$NON-NLS-1$
                { "1.1.1000.1/24", false }, //$NON-NLS-1$
                { "1.1.1.1000/24", false }, //$NON-NLS-1$
                { "1.1.1.1/33", false }, //$NON-NLS-1$
                { "1111.1.1.1/1", false }, //$NON-NLS-1$
                { "1111.1.1.1/32", false }, //$NON-NLS-1$
                { "1.1.1.1/1/1", false }, //$NON-NLS-1$
                { "1.1/1.1/.1/.1", false }, //$NON-NLS-1$
                { "256.1.1.1/1", false }, //$NON-NLS-1$
                { "256.1.1.1/32", false }, //$NON-NLS-1$
                { "256.1.1.1/222222222222222222222222", false }, //$NON-NLS-1$
                { "255.?.?././", false }, //$NON-NLS-1$
                { "255?23?1?0/8", false }, //$NON-NLS-1$
                { "23\22\22\22\22\\", false }, //$NON-NLS-1$
                { ".................", false }, //$NON-NLS-1$
                { "././././", false }, //$NON-NLS-1$
                { "?/?/?/?/", false }, //$NON-NLS-1$

                // Not A Network address
                { "253.0.0.32/26", false }, //$NON-NLS-1$
                { "255.255.255.192/25", false }, //$NON-NLS-1$
                { "255.255.255.16/24", false }, //$NON-NLS-1$
                { "255.254.192.0/17", false }, //$NON-NLS-1$
                { "255.255.255.250/16", false }, //$NON-NLS-1$
                { "255.255.192.17/14", false }, //$NON-NLS-1$
                { "255.128.0.0/8", false }, //$NON-NLS-1$
                { "240.255.255.247/3", false }, //$NON-NLS-1$
                { "224.0.0.0/2", false }, //$NON-NLS-1$
                { "192.0.0.0/1", false }, //$NON-NLS-1$
                { "255.255.255.255/0", false }, //$NON-NLS-1$

                // valid CIDR
                { "255.255.255.255/32", true }, //$NON-NLS-1$
                { "0.0.0.0/32", true }, //$NON-NLS-1$
                { "255.255.255.254/31", true }, //$NON-NLS-1$
                { "255.255.255.248/29", true }, //$NON-NLS-1$
                { "255.255.255.0/24", true }, //$NON-NLS-1$
                { "255.255.254.0/23", true }, //$NON-NLS-1$
                { "255.0.254.0/23", true }, //$NON-NLS-1$
                { "255.255.0.0/16", true }, //$NON-NLS-1$
                { "255.252.0.0/14", true }, //$NON-NLS-1$
                { "255.0.0.0/8", true }, //$NON-NLS-1$
                { "248.0.0.0/5", true }, //$NON-NLS-1$
                { "128.0.0.0/1", true }, //$NON-NLS-1$
        });
    }

}
