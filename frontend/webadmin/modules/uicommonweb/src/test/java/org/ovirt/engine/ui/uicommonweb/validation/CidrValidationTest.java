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

    private String cidr;
    private boolean expectedResult;

    public CidrValidationTest(String cidr, boolean expectedResult) {
        this.cidr = cidr;
        this.expectedResult = expectedResult;
    }

    @Before
    public void setup() {
        validation = spy(new CidrValidation());
        doReturn("").when(validation).getMessage();
    }

    @Test
    public void runTest() {
        assertEquals(expectedResult, validation.validate(cidr).getSuccess());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> comparisonParameters() {
        return Arrays.asList(new Object[][] {
                { null, false },
                { "", false }, //$NON-NLS-1$
                { "a", false }, //$NON-NLS-1$
                { "1", false }, //$NON-NLS-1$
                { "1.1.1.1", false }, //$NON-NLS-1$
                { "1.1.1.1/", false }, //$NON-NLS-1$
                { "1.1.1.1/40", false }, //$NON-NLS-1$
                { "1000.1.1.1/24", false }, //$NON-NLS-1$
                { "1.1000.1.1/24", false }, //$NON-NLS-1$
                { "1.1.1000.1/24", false }, //$NON-NLS-1$
                { "1.1.1.1000/24", false }, //$NON-NLS-1$

                { "0.0.0.0/0", true }, //$NON-NLS-1$
                { "1.1.1.1/0", true }, //$NON-NLS-1$
                { "1.1.1.1/24", true }, //$NON-NLS-1$
                { "1.1.1.1/32", true }, //$NON-NLS-1$
                { "100.1.1.1/24", true }, //$NON-NLS-1$
                { "255.1.1.1/24", true }, //$NON-NLS-1$
        });
    }

}
