package org.ovirt.engine.core.bll.qos;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RefreshNetworksParametersFactoryTest {
    @Mock
    private PersistentHostSetupNetworksParametersFactory persistentHostSetupNetworksParametersFactory;

    @Mock
    private NetworkDao networkDao;

    @Mock
    private VdsDao vdsDao;

    @InjectMocks
    private RefreshNetworksParametersFactory underTest;

    private static final Guid QOS_ID = Guid.newGuid();

    private Network networkA;
    private Network networkB;

    private VDS vdsA;
    private VDS vdsB;

    private List<Network> networksHavingQos;

    @BeforeEach
    public void setUp() {

        vdsA = createVds();
        vdsB = createVds();

        networkA = createNetwork();
        networkB = createNetwork();

        when(vdsDao.getAllForNetwork(networkA.getId())).thenReturn(Arrays.asList(vdsA, vdsB));
        when(vdsDao.getAllForNetwork(networkB.getId())).thenReturn(Collections.singletonList(vdsA));
        networksHavingQos = Arrays.asList(networkA, networkB);
    }

    @Test
    public void testCreateParametersToRefreshNetworksHavingQos() {
        when(networkDao.getAllForQos(QOS_ID)).thenReturn(networksHavingQos);
        underTest.create(QOS_ID);
        assertCreatedParameters();
    }

    @Test
    public void testCreateParametersToRefreshGivenNetworks() {
        underTest.create(networksHavingQos);
        assertCreatedParameters();
    }

    private void assertCreatedParameters() {
        assertCallToParametersCreationForVdsA();
        assertCallToParametersCreationForVdsB();
    }

    @SuppressWarnings("unchecked")
    private void assertCallToParametersCreationForVdsA() {
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(persistentHostSetupNetworksParametersFactory).create(eq(vdsA.getId()), captor.capture());
        assertCollectionContainingOnlyThese(captor.getValue(), networkA, networkB);
    }

    @SuppressWarnings("unchecked")
    private void assertCallToParametersCreationForVdsB() {
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(persistentHostSetupNetworksParametersFactory).create(eq(vdsB.getId()), captor.capture());
        assertCollectionContainingOnlyThese(captor.getValue(), networkA);
    }

    private void assertCollectionContainingOnlyThese(List<Network> actualNetworks, Network ... expectedNetworks) {
        assertThat(actualNetworks, Matchers.hasSize(expectedNetworks.length));
        assertThat(actualNetworks, containsInAnyOrder(expectedNetworks));
    }

    private Network createNetwork() {
        Network network = new Network();
        network.setId(Guid.newGuid());
        return network;
    }

    private VDS createVds() {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        return vds;
    }

}
