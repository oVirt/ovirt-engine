package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.dao.StoragePoolDAO;

/**
 * A test case for {@link GetDataCentersWithPermittedActionOnClusters}.
 * This test mocks away all the DAOs, and just tests the flow of the query itself.
 */
public class GetDataCentersWithPermittedActionOnClustersQueryTest
        extends AbstractGetEntitiesWithPermittedActionParametersQueryTest
        <GetEntitiesWithPermittedActionParameters, GetDataCentersWithPermittedActionOnClustersQuery<GetEntitiesWithPermittedActionParameters>> {

    @Test
    public void testQueryExecution() {
        // Set up the expected data
        storage_pool expected = new storage_pool();

        // Mock the DAO
        StoragePoolDAO storagePoolDAOMock = mock(StoragePoolDAO.class);
        when(storagePoolDAOMock.getDataCentersWithPermittedActionOnClusters(getUser().getUserId(), getActionGroup())).thenReturn(Collections.singletonList(expected));
        when(getDbFacadeMockInstance().getStoragePoolDAO()).thenReturn(storagePoolDAOMock);

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<storage_pool> actual = (List<storage_pool>) getQuery().getQueryReturnValue().getReturnValue();
        assertEquals("Wrong number of VDS Groups", 1, actual.size());
        assertEquals("Wrong VDS Groups", expected, actual.get(0));
    }
}
