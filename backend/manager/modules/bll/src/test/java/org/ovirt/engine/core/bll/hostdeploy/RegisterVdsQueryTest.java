package org.ovirt.engine.core.bll.hostdeploy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.queries.hostdeploy.RegisterVdsParameters;

/**
 * A test case for {@link RegisterVdsQuery}.
 */
public class RegisterVdsQueryTest extends AbstractUserQueryTest<RegisterVdsParameters, RegisterVdsQuery<RegisterVdsParameters>> {

    /**
     * A test for checking whether {@link RegisterVdsQuery#getStrippedVdsUniqueId()} method returns a valid VDS id which
     * contains only the valid characters. ^ is used as invalid character as defined in RegisterVdsQuery.
     */
    @Test
    public void testGetStrippedVdsUniqueIdWithUnacceptedChars() {
        String result = this.gerStrippedVdsUniqueId("Test_123");
        assertEquals("Test_123", result, "Vds id doesn't equal to the expected value");
    }

    /**
     * A test for checking whether {@link RegisterVdsQuery#getStrippedVdsUniqueId()} method returns a valid VDS id when
     * containing no invalid characters.
     */
    @Test
    public void testGetStrippedVdsUniqueIdOnlyAllowrdChars() {
        String result = this.gerStrippedVdsUniqueId("Test_123");
        assertEquals("Test_123", result, "Vds id doesn't equal to the expected value");
    }

    /**
     * A test for checking whether {@link RegisterVdsQuery#getStrippedVdsUniqueId()} method returns an empty VDS id when
     * containing only invalid characters.
     */
    @Test
    public void testGetStrippedVdsUniqueIdWithoutValidChars() {
        String result = this.gerStrippedVdsUniqueId("^%^");
        assertEquals("", result, "Vds id is not empty as expected");
    }

    private String gerStrippedVdsUniqueId(String vdsId) {
        RegisterVdsParameters paramsMock = getQueryParameters();
        when(paramsMock.getVdsUniqueId()).thenReturn(vdsId);
        return getQuery().getStrippedVdsUniqueId();
    }
}
