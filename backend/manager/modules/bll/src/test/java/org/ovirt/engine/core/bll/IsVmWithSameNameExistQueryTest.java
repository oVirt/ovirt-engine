package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.queries.IsVmWithSameNameExistParameters;
import org.ovirt.engine.core.utils.RandomUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/** A test case for the {@link IsVmWithSameNameExistQuery} class. */
@RunWith(PowerMockRunner.class)
@PrepareForTest(VmHandler.class)
public class IsVmWithSameNameExistQueryTest extends AbstractQueryTest<IsVmWithSameNameExistParameters, IsVmWithSameNameExistQuery<IsVmWithSameNameExistParameters>> {

    @Test
    public void testExecuteQuery() {
        // Mock the parameters
        String vmName = RandomUtils.instance().nextString(10);
        when(getQueryParameters().getVmName()).thenReturn(vmName);

        // Mock the result
        boolean result = RandomUtils.instance().nextBoolean();
        mockStatic(VmHandler.class);
        when(VmHandler.isVmWithSameNameExistStatic(vmName)).thenReturn(result);

        // Execute the query
        getQuery().executeQueryCommand();

        // Assert the result
        assertEquals("Wrong result", result, getQuery().getQueryReturnValue().getReturnValue());
    }
}
