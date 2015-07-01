package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsGroupDao;

/** A test case for {@link GetVdsGroupsByStoragePoolIdQuery} */
public class GetVdsGroupsByStoragePoolIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetVdsGroupsByStoragePoolIdQuery<IdQueryParameters>> {

    /** Tests the flow of {@link GetVdsGroupsByStoragePoolIdQuery#executeQueryCommand()} using mock objects */
    @Test
    public void testExecuteQueryCommand() {
        // Set up the result
        Guid storagePoolId = Guid.newGuid();
        VDSGroup group = new VDSGroup();
        group.setStoragePoolId(storagePoolId);
        List<VDSGroup> result = Collections.singletonList(group);

        // Set up the query parameters
        when(getQueryParameters().getId()).thenReturn(storagePoolId);

        // Mock the Dao
        VdsGroupDao vdsGroupDaoMock = mock(VdsGroupDao.class);
        when(vdsGroupDaoMock.getAllForStoragePool(storagePoolId,
                getUser().getId(),
                getQueryParameters().isFiltered())).
                thenReturn(result);
        when(getDbFacadeMockInstance().getVdsGroupDao()).thenReturn(vdsGroupDaoMock);

        // Execute the query
        getQuery().executeQueryCommand();

        // Check the result
        assertEquals("Wrong query result", result, getQuery().getQueryReturnValue().getReturnValue());
    }
}
