package org.ovirt.engine.core.bll.network.dc;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.network.dc.AddNetworkCommand.AddNetworkValidator;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.RandomUtils;

@RunWith(MockitoJUnitRunner.class)
public class AddNetworkValidatorTest {

    @Mock
    private Network network;

    @Mock
    private DbFacade dbFacade;

    @Mock
    private NetworkDao networkDao;

    @Mock
    private VmDao vmDao;

    private List<Network> networks = new ArrayList<>();
    private AddNetworkValidator validator;

    @Before
    public void setup() {
        validator = spy(new AddNetworkValidator(vmDao, network));

        // spy on attempts to access the database
        doReturn(dbFacade).when(validator).getDbFacade();

        // mock some commonly used Daos
        when(dbFacade.getNetworkDao()).thenReturn(networkDao);

        // mock their getters
        when(networkDao.getAllForDataCenter(any(Guid.class))).thenReturn(networks);
    }

    private void externalNetworkNewInDataCenterTestSetup(boolean equalToNetwork) {
        Network externalNetwork = mock(Network.class);
        ProviderNetwork providerNetwork = mock(ProviderNetwork.class);
        when(network.getProvidedBy()).thenReturn(providerNetwork);

        if (equalToNetwork) {
            when(externalNetwork.getProvidedBy()).thenReturn(providerNetwork);
        }

        networks.add(externalNetwork);
    }

    @Test
    public void externalNetworkIsNewInDataCenterNoNetworks() throws Exception {
        assertThat(validator.externalNetworkNewInDataCenter(), isValid());
    }

    @Test
    public void externalNetworkIsNewInDataCenter() throws Exception {
        externalNetworkNewInDataCenterTestSetup(false);
        assertThat(validator.externalNetworkNewInDataCenter(), isValid());
    }

    @Test
    public void externalNetworkIsNotNewInDataCenter() throws Exception {
        externalNetworkNewInDataCenterTestSetup(true);
        assertThat(validator.externalNetworkNewInDataCenter(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_ALREADY_EXISTS));
    }

    @Test
    public void externalNetworkIsAVmNetwork() throws Exception {
        when(network.isVmNetwork()).thenReturn(true);
        assertThat(validator.externalNetworkIsVmNetwork(), isValid());
    }

    @Test
    public void externalNetworkIsNotAVmNetwork() throws Exception {
        when(network.isVmNetwork()).thenReturn(false);
        assertThat(validator.externalNetworkIsVmNetwork(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_MUST_BE_VM_NETWORK));
    }

    @Test
    public void externalNetworkVlanValid() {
        when(network.getVlanId()).thenReturn(RandomUtils.instance().nextInt());
        when(network.getLabel()).thenReturn(RandomUtils.instance().nextString(10));
        assertThat(validator.externalNetworkVlanValid(), isValid());
    }

    @Test
    public void externalNetworkVlanInvalid() {
        when(network.getVlanId()).thenReturn(RandomUtils.instance().nextInt());
        assertThat(validator.externalNetworkVlanValid(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_WITH_VLAN_MUST_BE_LABELED));
    }

    @Test
    public void externalNetworkNoVlanWithLabel() {
        when(network.getLabel()).thenReturn(RandomUtils.instance().nextString(10));
        assertThat(validator.externalNetworkVlanValid(), isValid());
    }
}
