package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;

/** A test case for {@link GetClustersByStoragePoolIdQuery} */
public class GetClustersByStoragePoolIdQueryTest
        extends AbstractUserQueryTest<IdQueryParameters, GetClustersByStoragePoolIdQuery<IdQueryParameters>> {

    /** Tests the flow of {@link GetClustersByStoragePoolIdQuery#executeQueryCommand()} using mock objects */
    @Test
    public void testExecuteQueryCommand() {
        // Set up the result
        Guid storagePoolId = Guid.newGuid();
        Cluster group = new Cluster();
        group.setStoragePoolId(storagePoolId);
        List<Cluster> result = Collections.singletonList(group);

        // Set up the query parameters
        when(getQueryParameters().getId()).thenReturn(storagePoolId);

        // Mock the Dao
        ClusterDao clusterDaoMock = mock(ClusterDao.class);
        when(clusterDaoMock.getAllForStoragePool(storagePoolId,
                getUser().getId(),
                getQueryParameters().isFiltered())).
                thenReturn(result);
        when(getDbFacadeMockInstance().getClusterDao()).thenReturn(clusterDaoMock);

        // Execute the query
        getQuery().executeQueryCommand();

        // Check the result
        assertEquals("Wrong query result", result, getQuery().getQueryReturnValue().getReturnValue());
    }
}
