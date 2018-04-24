package org.ovirt.engine.core.bll.network.dc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.network.dc.AddNetworkCommand.AddNetworkValidator;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.InjectorExtension;

@ExtendWith({MockitoExtension.class, InjectorExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class AddNetworkValidatorTest {

    @Mock
    private Network network;

    @Mock
    private ProviderNetwork providerNetwork;

    @Mock
    @InjectedMock
    public NetworkDao networkDao;

    private List<Network> networks = new ArrayList<>();
    private AddNetworkValidator validator;

    @BeforeEach
    public void setup() {
        validator = new AddNetworkValidator(network);

        when(network.getProvidedBy()).thenReturn(providerNetwork);

        // mock DAO getters
        when(networkDao.getAllForDataCenter(any())).thenReturn(networks);
    }

    private void externalNetworkNewInDataCenterTestSetup(boolean equalToNetwork) {
        Network externalNetwork = mock(Network.class);


        if (equalToNetwork) {
            when(externalNetwork.getProvidedBy()).thenReturn(providerNetwork);
        }

        networks.add(externalNetwork);
    }

    @Test
    public void externalNetworkIsNewInDataCenterNoNetworks() {
        assertThat(validator.externalNetworkNewInDataCenter(), isValid());
    }

    @Test
    public void externalNetworkIsNewInDataCenter() {
        externalNetworkNewInDataCenterTestSetup(false);
        assertThat(validator.externalNetworkNewInDataCenter(), isValid());
    }

    @Test
    public void externalNetworkIsNotNewInDataCenter() {
        externalNetworkNewInDataCenterTestSetup(true);
        assertThat(validator.externalNetworkNewInDataCenter(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_ALREADY_EXISTS));
    }

    @Test
    public void externalNetworkIsAVmNetwork() {
        when(network.isVmNetwork()).thenReturn(true);
        assertThat(validator.externalNetworkIsVmNetwork(), isValid());
    }

    @Test
    public void externalNetworkIsNotAVmNetwork() {
        when(network.isVmNetwork()).thenReturn(false);
        assertThat(validator.externalNetworkIsVmNetwork(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_MUST_BE_VM_NETWORK));
    }

    @Test
    public void externalNetworkVlanValid() {
        when(providerNetwork.hasExternalVlanId()).thenReturn(true);
        when(providerNetwork.hasCustomPhysicalNetworkName()).thenReturn(true);
        assertThat(validator.externalNetworkVlanValid(), isValid());
    }

    @Test
    public void externalNetworkVlanInvalid() {
        when(providerNetwork.hasExternalVlanId()).thenReturn(true);
        assertThat(validator.externalNetworkVlanValid(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_WITH_VLAN_MUST_BE_CUSTOM));
    }

    @Test
    public void externalNetworkNoVlanWithCustomNetwork() {
        when(providerNetwork.hasCustomPhysicalNetworkName()).thenReturn(true);
        assertThat(validator.externalNetworkVlanValid(), isValid());
    }
}
