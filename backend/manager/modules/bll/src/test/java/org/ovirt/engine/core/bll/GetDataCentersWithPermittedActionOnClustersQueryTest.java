package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.GetDataCentersWithPermittedActionOnClustersParameters;
import org.ovirt.engine.core.dao.StoragePoolDao;

/**
 * A test case for {@link GetDataCentersWithPermittedActionOnClusters}.
 * This test mocks away all the Daos, and just tests the flow of the query itself.
 */
public class GetDataCentersWithPermittedActionOnClustersQueryTest
        extends AbstractGetEntitiesWithPermittedActionParametersQueryTest
        <GetDataCentersWithPermittedActionOnClustersParameters, GetDataCentersWithPermittedActionOnClustersQuery<GetDataCentersWithPermittedActionOnClustersParameters>> {

    @Mock
    private StoragePoolDao storagePoolDaoMock;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        when(((GetDataCentersWithPermittedActionOnClustersParameters) getQueryParameters()).isSupportsVirtService()).thenReturn(true);
        when(((GetDataCentersWithPermittedActionOnClustersParameters) getQueryParameters()).isSupportsGlusterService()).thenReturn(false);
    }

    @Test
    public void testQueryExecution() {
        // Set up the expected data
        StoragePool expected = new StoragePool();

        // Mock the Dao
        when(storagePoolDaoMock.getDataCentersWithPermittedActionOnClusters(getUser().getId(), getActionGroup(), true, false)).thenReturn(Collections.singletonList(expected));

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<StoragePool> actual = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(1, actual.size(), "Wrong number of VDS Groups");
        assertEquals(expected, actual.get(0), "Wrong VDS Groups");
    }
}
