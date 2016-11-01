package org.ovirt.engine.core.bll.storage.pool;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StoragePoolDao;

/**
 * A test case for {@link GetStoragePoolByIdQuery}.
 * It does not test database implementation, but rather tests that the right delegations to the Dao occur.
 */
public class GetStoragePoolByIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetStoragePoolByIdQuery<IdQueryParameters>> {

    @Mock
    private StoragePoolDao storagePoolDaoMock;

    @Mock
    private ClusterDao clusterDaoMock;

    @Test
    public void testExecuteQueryWhenAllClustersHaveSameMacPoolAssigned() {
        test(true);
    }

    @Test
    public void testExecuteQueryWhenAllClustersHaveNotSameMacPoolAssigned() {
        test(false);
    }

    private void test(boolean allClustersUsesSamePool) {
        Guid storagePoolID = Guid.newGuid();
        StoragePool expectedResult = new StoragePool();
        expectedResult.setId(storagePoolID);

        IdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getId()).thenReturn(storagePoolID);

        when(storagePoolDaoMock.get(storagePoolID, getUser().getId(), paramsMock.isFiltered()))
                .thenReturn(expectedResult);

        final Guid cluster1Guid = Guid.newGuid();
        final Cluster cluster1 = new Cluster();
        cluster1.setMacPoolId(cluster1Guid);

        final Cluster cluster2 = new Cluster();
        cluster2.setMacPoolId(allClustersUsesSamePool ? cluster1Guid : Guid.newGuid());

        when(clusterDaoMock.getAllForStoragePool(storagePoolID)).thenReturn(Arrays.asList(cluster1, cluster2));

        getQuery().executeQueryCommand();

        StoragePool result = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals("Wrong storage pool returned", expectedResult, result);
        assertThat(result.getMacPoolId(), allClustersUsesSamePool ? is(cluster1Guid) : CoreMatchers.nullValue());
    }
}
