package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;

/**
 * A test case for {@link GetVmsRunningOnOrMigratingToVdsQuery}. This test mocks away all the Daos, and just tests the
 * flow of the query itself.
 */
public class GetVmsRunningOnOrMigratingToVdsQueryTest
        extends AbstractQueryTest<IdQueryParameters,
        GetVmsRunningOnOrMigratingToVdsQuery<IdQueryParameters>> {

    @Mock
    private VmDao vmDaoMock;

    @Test
    public void testQueryExecution() {
        Guid vmGuid = Guid.newGuid();

        VM vm = new VM();
        vm.setId(vmGuid);

        List<VM> expected = Collections.singletonList(vm);
        when(vmDaoMock.getAllRunningOnOrMigratingToVds(vmGuid)).thenReturn(expected);

        doNothing().when(getQuery()).updateStatistics(expected);
        doNothing().when(getQuery()).updateConfiguredCpuVerb(expected);

        // Set up the query parameters
        when(getQueryParameters().getId()).thenReturn(vmGuid);

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<VM> actual = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(1, actual.size(), "Wrong number of VMs");
    }
}
