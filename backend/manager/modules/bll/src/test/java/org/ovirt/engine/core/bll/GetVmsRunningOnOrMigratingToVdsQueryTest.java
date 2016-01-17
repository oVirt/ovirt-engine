package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
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

    @Test
    public void testQueryExecution() {
        Guid vmGuid = Guid.newGuid();

        VM vm = new VM();
        vm.setId(vmGuid);

        List<VM> expected = Collections.singletonList(vm);
        VmDao vmDaoMock = mock(VmDao.class);
        when(vmDaoMock.getAllRunningOnOrMigratingToVds(vmGuid)).thenReturn(expected);
        when(getDbFacadeMockInstance().getVmDao()).thenReturn(vmDaoMock);
        // Set up the query parameters
        when(getQueryParameters().getId()).thenReturn(vmGuid);

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<VM> actual = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals("Wrong number of VMs", 1, actual.size());
    }
}
