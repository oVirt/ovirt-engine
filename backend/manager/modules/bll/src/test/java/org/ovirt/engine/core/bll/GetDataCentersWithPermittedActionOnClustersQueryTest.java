package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.GetDataCentersWithPermittedActionOnClustersParameters;
import org.ovirt.engine.core.dao.StoragePoolDAO;

/**
 * A test case for {@link GetDataCentersWithPermittedActionOnClusters}.
 * This test mocks away all the DAOs, and just tests the flow of the query itself.
 */
public class GetDataCentersWithPermittedActionOnClustersQueryTest
        extends AbstractGetEntitiesWithPermittedActionParametersQueryTest
        <GetDataCentersWithPermittedActionOnClustersParameters, GetDataCentersWithPermittedActionOnClustersQuery<GetDataCentersWithPermittedActionOnClustersParameters>> {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(((GetDataCentersWithPermittedActionOnClustersParameters) getQueryParameters()).isSupportsVirtService()).thenReturn(true);
        when(((GetDataCentersWithPermittedActionOnClustersParameters) getQueryParameters()).isSupportsGlusterService()).thenReturn(false);
    }

    @Test
    public void testQueryExecution() {
        // Set up the expected data
        storage_pool expected = new storage_pool();

        // Mock the DAO
        StoragePoolDAO storagePoolDAOMock = mock(StoragePoolDAO.class);
        when(storagePoolDAOMock.getDataCentersWithPermittedActionOnClusters(getUser().getUserId(), getActionGroup(), true, false)).thenReturn(Collections.singletonList(expected));
        when(getDbFacadeMockInstance().getStoragePoolDao()).thenReturn(storagePoolDAOMock);

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<storage_pool> actual = (List<storage_pool>) getQuery().getQueryReturnValue().getReturnValue();
        assertEquals("Wrong number of VDS Groups", 1, actual.size());
        assertEquals("Wrong VDS Groups", expected, actual.get(0));
    }
}
