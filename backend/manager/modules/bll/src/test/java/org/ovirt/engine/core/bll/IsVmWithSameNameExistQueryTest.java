package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.utils.RandomUtils;

/** A test case for the {@link IsVmWithSameNameExistQuery} class. */
public class IsVmWithSameNameExistQueryTest extends AbstractUserQueryTest<NameQueryParameters, IsVmWithSameNameExistQuery<NameQueryParameters>> {

    @Test
    public void testExecuteQuery() {
        // Mock the parameters
        String vmName = RandomUtils.instance().nextString(10);
        when(getQueryParameters().getName()).thenReturn(vmName);

        // Mock the result
        boolean result = RandomUtils.instance().nextBoolean();
        doReturn(result).when(getQuery()).isVmWithSameNameExistStatic(vmName, null);

        // Execute the query
        getQuery().executeQueryCommand();

        // Assert the result
        assertEquals("Wrong result", result, getQuery().getQueryReturnValue().getReturnValue());
    }
}
