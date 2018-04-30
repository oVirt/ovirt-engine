package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
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
        assertEquals(result, getQuery().getQueryReturnValue().getReturnValue(), "Wrong result");
    }
}
