package org.ovirt.engine.core.bll.network.cluster;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;

@ExtendWith(MockitoExtension.class)
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
    @Mock
    private Predicate<Network> mockManagementNetworkCandidatePredicate;

    private DefaultManagementNetworkFinderImpl underTest;

    @BeforeEach
    public void setUp() {
        underTest = new DefaultManagementNetworkFinderImpl(
                mockNetworkDao,
                mockManagementNetworkUtil,
                mockManagementNetworkCandidatePredicate);
    }

    @Test
    public void testFindDefaultManagementNetworkDefault() {
        when(mockManagementNetworkUtil.getDefaultManagementNetworkName()).thenReturn(DEFAULT_ENGINE_NETWORK_NAME);
        when(mockNetworkDao.getByNameAndDataCenter(DEFAULT_ENGINE_NETWORK_NAME, TEST_DC_ID)).thenReturn(mockDefaultEngineNetwork);

        final Network actual = underTest.findDefaultManagementNetwork(TEST_DC_ID);

        assertSame(actual, mockDefaultEngineNetwork);
    }

    @Test
    public void testFindDefaultManagementNetworkNonDefault() {
        when(mockNetworkDao.getAllForDataCenter(TEST_DC_ID)).thenReturn(Arrays.asList(mockNetwork, mockNetwork));
        when(mockNetworkDao.getManagementNetworks(TEST_DC_ID))
                .thenReturn(Collections.singletonList(mockNetwork));

        final Network actual = underTest.findDefaultManagementNetwork(TEST_DC_ID);

        assertSame(actual, mockNetwork);
    }

    @Test
    public void testFindDefaultManagementNetworkMultipleNonDefault() {
        when(mockNetworkDao.getAllForDataCenter(TEST_DC_ID)).thenReturn(Arrays.asList(mockNetwork, mockNetwork));
        when(mockNetworkDao.getManagementNetworks(TEST_DC_ID))
                .thenReturn(Arrays.asList(mockNetwork, mockDefaultEngineNetwork));

        final Network actual = underTest.findDefaultManagementNetwork(TEST_DC_ID);

        assertNull(actual);
    }

    @Test
    public void testFindDefaultManagementNetworkNotFound() {

        final Network actual = underTest.findDefaultManagementNetwork(TEST_DC_ID);

        assertNull(actual);
    }

    @Test
    public void testFindSingleDcNetwork() {
        when(mockNetworkDao.getAllForDataCenter(TEST_DC_ID)).thenReturn(Collections.singletonList(mockNetwork));
        when(mockManagementNetworkCandidatePredicate.test(mockNetwork)).thenReturn(true);

        final Network actual = underTest.findDefaultManagementNetwork(TEST_DC_ID);

        assertSame(actual, mockNetwork);
    }

    @Test
    public void testFindSingleNotAppropriateDcNetwork() {
        when(mockNetworkDao.getAllForDataCenter(TEST_DC_ID)).thenReturn(Collections.singletonList(mockNetwork));
        when(mockManagementNetworkCandidatePredicate.test(mockNetwork)).thenReturn(false);

        final Network actual = underTest.findDefaultManagementNetwork(TEST_DC_ID);

        assertNull(actual);
    }

    @Test
    public void testFindMultipleDcNetworks() {
        when(mockNetworkDao.getAllForDataCenter(TEST_DC_ID)).thenReturn(Arrays.asList(mockNetwork, mockNetwork));

        final Network actual = underTest.findDefaultManagementNetwork(TEST_DC_ID);

        assertNull(actual);
    }
}
