package org.ovirt.engine.core.bll.exportimport;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;

@RunWith(MockitoJUnitRunner.class)
public class ImportedNetworkInfoUpdaterTest {

    private static final String EXTERNAL_NETWORK_NAME = "external network name";
    private static final String TARGET_NETWORK_NAME = "target network name";
    private static final String VNIC_PROFILE_NAME = "vnic profile name";
    private static final Guid VNIC_PROFILE_ID = Guid.newGuid();
    private static final Guid TARGET_NETWORK_ID = Guid.newGuid();

    @InjectMocks
    private ImportedNetworkInfoUpdater underTest;

    @Mock
    private NetworkDao mockNetworkDao;

    @Mock
    private TargetVnicProfileFinder mockTargetVnicProfileFinder;

    @Mock
    private VmNetworkInterface mockVmNetworkInterface;

    private VnicProfile vnicProfile;

    private List<ExternalVnicProfileMapping> externalVnicProfileMappings;
    private Network targetNetwork;

    @Before
    public void setUp() {
        externalVnicProfileMappings = new ArrayList<>();
        vnicProfile = new VnicProfile();
        vnicProfile.setId(VNIC_PROFILE_ID);
        vnicProfile.setName(VNIC_PROFILE_NAME);
        vnicProfile.setNetworkId(TARGET_NETWORK_ID);

        targetNetwork = new Network();
        targetNetwork.setName(TARGET_NETWORK_NAME);

        when(mockVmNetworkInterface.getNetworkName()).thenReturn(EXTERNAL_NETWORK_NAME);
        when(mockVmNetworkInterface.getVnicProfileName()).thenReturn(VNIC_PROFILE_NAME);
    }

    @Test
    public void testUpdateNetworkInfoMappingNotFound() {
        underTest.updateNetworkInfo(mockVmNetworkInterface, externalVnicProfileMappings);

        verify(mockTargetVnicProfileFinder).findTargetVnicProfile(EXTERNAL_NETWORK_NAME,
                VNIC_PROFILE_NAME,
                externalVnicProfileMappings);
        verify(mockVmNetworkInterface, never()).setVnicProfileId(any());
        verify(mockVmNetworkInterface, never()).setNetworkName(any());
        verify(mockVmNetworkInterface, never()).setVnicProfileName(any());
    }

    @Test
    public void testUpdateNetworkInfo() {
        when(mockTargetVnicProfileFinder.findTargetVnicProfile(
                EXTERNAL_NETWORK_NAME,
                VNIC_PROFILE_NAME,
                externalVnicProfileMappings))
                .thenReturn(vnicProfile);
        when(mockNetworkDao.get(TARGET_NETWORK_ID)).thenReturn(targetNetwork);

        underTest.updateNetworkInfo(mockVmNetworkInterface, externalVnicProfileMappings);

        verify(mockTargetVnicProfileFinder).findTargetVnicProfile(EXTERNAL_NETWORK_NAME,
                VNIC_PROFILE_NAME,
                externalVnicProfileMappings);
        verify(mockVmNetworkInterface).setVnicProfileId(VNIC_PROFILE_ID);
        verify(mockVmNetworkInterface).setVnicProfileName(VNIC_PROFILE_NAME);
        verify(mockVmNetworkInterface).setNetworkName(TARGET_NETWORK_NAME);
    }
}
