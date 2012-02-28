package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.queries.GetAllVmPoolsAttachedToUserParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmPoolDAO;

/**
 * A test class for the {@link GetAllVmPoolsAttachedToUserQuery} class.
 * It mocks the DAO and tests the flow of the query itself.
 */
public class GetAllVmPoolsAttachedToUserQueryTest extends AbstractUserQueryTest<GetAllVmPoolsAttachedToUserParameters, GetAllVmPoolsAttachedToUserQuery<GetAllVmPoolsAttachedToUserParameters>> {

    /** Tests that executing a query with the same user works */
    @Test
    public void testQueryWithSameUser() {
        assertExecuteQueryCommandResult(getUser().getUserId(), true);
    }

    /** Tests that executing a query with a different user returns an empty list */
    @Test
    public void testQueryWithOtherUser() {
        assertExecuteQueryCommandResult(Guid.NewGuid(), false);
    }

    /** Tests that executing a query with a different user works when the query is run in admin mode */
    @Test
    public void testAdminQueryWithOtherUserWithDisks() {
        when(getQueryParameters().isFiltered()).thenReturn(false);
        assertExecuteQueryCommandResult(Guid.NewGuid(), true);
    }

    public void assertExecuteQueryCommandResult(Guid requestedUser, boolean expectedResults) {
        mockQueryParameters(requestedUser);

        // Mock the result of the DAO
        final vm_pools expectedPool = mockVMPoolsFromDAO(requestedUser);

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<vm_pools> actualPools = (List<vm_pools>) getQuery().getQueryReturnValue().getReturnValue();
        if (!expectedResults) {
            assertTrue("no VMs should have been returned", actualPools.isEmpty());
        } else {
            assertEquals("wrong number of VMs returned", 1, actualPools.size());
            vm_pools actualPool = actualPools.get(0);
            assertEquals("wrong VMs returned", expectedPool, actualPool);
        }
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
    private vm_pools mockVMPoolsFromDAO(Guid requestedUser) {
        vm_pools expectedPool = new vm_pools();
        VmPoolDAO vmDaoPoolMock = mock(VmPoolDAO.class);
        when(vmDaoPoolMock.getAllForUser(requestedUser)).thenReturn(Collections.singletonList(expectedPool));
        when(getDbFacadeMockInstance().getVmPoolDAO()).thenReturn(vmDaoPoolMock);

        return expectedPool;
    }
}
