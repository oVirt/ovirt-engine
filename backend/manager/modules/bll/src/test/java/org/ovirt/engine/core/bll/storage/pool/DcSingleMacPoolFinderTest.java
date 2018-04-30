package org.ovirt.engine.core.bll.storage.pool;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;

@ExtendWith(MockitoExtension.class)
public class DcSingleMacPoolFinderTest {

    private static final Guid DC_ID = Guid.newGuid();
    private static final Guid MAC_POOL_ID1 = Guid.newGuid();
    private static final Guid MAC_POOL_ID2 = Guid.newGuid();

    @Mock
    private ClusterDao mockClusterDao;

    @InjectMocks
    private DcSingleMacPoolFinder underTest;

    @Test
    public void testFindSingleMacPool() {
        when(mockClusterDao.getAllForStoragePool(DC_ID)).thenReturn(createClustersWithMacPoolIds(MAC_POOL_ID1));

        final Guid actual = underTest.find(DC_ID);

        assertThat(actual, is(MAC_POOL_ID1));
    }

    @Test
    public void testFindMultipleMacPools() {
        when(mockClusterDao.getAllForStoragePool(DC_ID))
                .thenReturn(createClustersWithMacPoolIds(MAC_POOL_ID1, MAC_POOL_ID2));

        final Guid actual = underTest.find(DC_ID);

        assertThat(actual, nullValue());
    }

    @Test
    public void testFindNoClustersUnderDc() {
        when(mockClusterDao.getAllForStoragePool(DC_ID)).thenReturn(emptyList());

        final Guid actual = underTest.find(DC_ID);

        assertThat(actual, nullValue());
    }

    private List<Cluster> createClustersWithMacPoolIds(Guid... macPoolIds) {
        return stream(macPoolIds).map(macPoolId -> {
            final Cluster cluster = new Cluster();
            cluster.setMacPoolId(macPoolId);
            return cluster;
        }).collect(toList());
    }

}
