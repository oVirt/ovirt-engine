package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.queries.GetAllVmPoolsAttachedToUserParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmPoolDAO;

/**
 * A test class for the {@link GetAllVmPoolsAttachedToUserQuery} class.
 * It mocks the DAO and tests the flow of the query itself.
 */
public class GetAllVmPoolsAttachedToUserQueryTest extends AbstractQueryTest<GetAllVmPoolsAttachedToUserParameters, GetAllVmPoolsAttachedToUserQuery<GetAllVmPoolsAttachedToUserParameters>> {

    /** Tests that executing a query returns the expected result */
    @Test
    public void testAdminQueryWithOtherUserWithDisks() {
        Guid requestedUser = Guid.NewGuid();
        mockQueryParameters(requestedUser);

        // Mock the result of the DAO
        final VmPool expectedPool = mockVMPoolsFromDAO(requestedUser);

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<VmPool> actualPools = (List<VmPool>) getQuery().getQueryReturnValue().getReturnValue();
        assertEquals("wrong number of VMs returned", 1, actualPools.size());
        VmPool actualPool = actualPools.get(0);
        assertEquals("wrong VMs returned", expectedPool, actualPool);
    }

    /**
     * Adds additional parameters to the parameters object
     * @param requestedUser The user to get the VMs for
     */
    private void mockQueryParameters(Guid requestedUser) {
        when(getQueryParameters().getUserId()).thenReturn(requestedUser);
    }

    /**
     * Mocks the DAOs to return a VM
     * @param requestedUser The user on the parameter object to return the VM for
     * @return The VM the mocked DAO will return
     */
    private VmPool mockVMPoolsFromDAO(Guid requestedUser) {
        VmPool expectedPool = new VmPool();
        VmPoolDAO vmDaoPoolMock = mock(VmPoolDAO.class);
        when(vmDaoPoolMock.getAllForUser(requestedUser)).thenReturn(Collections.singletonList(expectedPool));
        when(getDbFacadeMockInstance().getVmPoolDao()).thenReturn(vmDaoPoolMock);

        return expectedPool;
    }
}
