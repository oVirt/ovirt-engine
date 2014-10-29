package org.ovirt.engine.core.bll.network.cluster;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultManagementNetworkFinderImplTest {

    private static final String DEFAULT_ENGINE_NETWORK_NAME = "test";

    private static final Guid TEST_DC_ID = Guid.Empty;

    @Mock
    private NetworkDao mockNetworkDao;
    @Mock
    private Network mockNetwork;
    @Mock
    private Network mockDefaultEngineNetwork;
    @Mock
    private ManagementNetworkUtil mockManagementNetworkUtil;

    private DefaultManagementNetworkFinderImpl underTest;

    @Before
    public void setUp() {
        underTest = new DefaultManagementNetworkFinderImpl(mockNetworkDao, mockManagementNetworkUtil);
    }

    @Test
    public void testFindDefaultManagementNetworkDefault() throws Exception {
        when(mockManagementNetworkUtil.getDefaultManagementNetworkName()).thenReturn(DEFAULT_ENGINE_NETWORK_NAME);
        when(mockNetworkDao.getByNameAndDataCenter(DEFAULT_ENGINE_NETWORK_NAME, TEST_DC_ID)).thenReturn(mockDefaultEngineNetwork);

        final Network actual = underTest.findDefaultManagementNetwork(TEST_DC_ID);

        assertSame(actual, mockDefaultEngineNetwork);
    }

    @Test
    public void testFindDefaultManagementNetworkNonDefault() throws Exception {
        when(mockNetworkDao.getManagementNetworks(TEST_DC_ID))
                .thenReturn(Collections.singletonList(mockNetwork));

        final Network actual = underTest.findDefaultManagementNetwork(TEST_DC_ID);

        assertSame(actual, mockNetwork);
    }

    @Test
    public void testFindDefaultManagementNetworkMultipleNonDefault() throws Exception {
        when(mockNetworkDao.getManagementNetworks(TEST_DC_ID))
                .thenReturn(Arrays.asList(mockNetwork, mockDefaultEngineNetwork));

        final Network actual = underTest.findDefaultManagementNetwork(TEST_DC_ID);

        assertNull(actual);
    }

    @Test
    public void testFindDefaultManagementNetworkNotFound() throws Exception {

        final Network actual = underTest.findDefaultManagementNetwork(TEST_DC_ID);

        assertNull(actual);
    }

}
