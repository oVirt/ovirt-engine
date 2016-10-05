package org.ovirt.engine.core.bll.exportimport;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;

@RunWith(MockitoJUnitRunner.class)
public class VnicProfileValidatorTest {

    private static final Guid VNIC_PROFILE_ID = Guid.newGuid();
    private static final Guid CLUSTER_ID = Guid.newGuid();
    private static final Guid NETWORK_ID = Guid.newGuid();

    @InjectMocks
    private VnicProfileValidator underTest;

    @Mock
    private NetworkClusterDao mockNetworkClusterDao;
    @Mock
    private VnicProfileDao mockVnicProfileDao;

    private VnicProfile vnicProfile;
    private List<NetworkCluster> targetClusterNetworks;
    private NetworkCluster networkCluster1;
    private NetworkCluster networkCluster2;

    @Before
    public void setUp() {
        vnicProfile = new VnicProfile();
        vnicProfile.setNetworkId(NETWORK_ID);

        networkCluster1 = new NetworkCluster();
        networkCluster2 = new NetworkCluster();
        networkCluster1.setNetworkId(Guid.Empty);
        networkCluster2.setNetworkId(NETWORK_ID);
        targetClusterNetworks = new ArrayList<>();

        when(mockNetworkClusterDao.getAllForCluster(CLUSTER_ID)).thenReturn(targetClusterNetworks);
    }

    @Test
    public void testVnicProfileBelongsToClusterPositive() {
        when(mockVnicProfileDao.get(VNIC_PROFILE_ID)).thenReturn(vnicProfile);
        when(mockNetworkClusterDao.getAllForCluster(CLUSTER_ID)).thenReturn(targetClusterNetworks);
        targetClusterNetworks.addAll(Arrays.asList(networkCluster1, networkCluster2));

        final ValidationResult actual = underTest.validateTargetVnicProfileId(VNIC_PROFILE_ID, CLUSTER_ID);

        assertThat(actual, isValid());
    }

    @Test
    public void testVnicProfileBelongsToClusterUnknownVnicProfileId() {
        final ValidationResult actual = underTest.validateTargetVnicProfileId(VNIC_PROFILE_ID, CLUSTER_ID);

        assertThat(actual, failsWith(EngineMessage.ACTION_TYPE_FAILED_VNIC_PROFILE_NOT_EXISTS));
    }

    @Test
    public void testVnicProfileBelongsToClusterNegative() {
        when(mockVnicProfileDao.get(VNIC_PROFILE_ID)).thenReturn(vnicProfile);
        targetClusterNetworks.add(networkCluster1);

        final ValidationResult actual = underTest.validateTargetVnicProfileId(VNIC_PROFILE_ID, CLUSTER_ID);

        assertThat(actual, failsWith(EngineMessage.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER));
    }
}
