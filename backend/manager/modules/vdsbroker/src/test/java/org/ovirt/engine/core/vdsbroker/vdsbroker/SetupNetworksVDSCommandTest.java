package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.vdscommands.SetupNetworksVdsCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

@RunWith(MockitoJUnitRunner.class)
public class SetupNetworksVDSCommandTest {

    @Mock
    private IVdsServer server;

    @Captor
    private ArgumentCaptor<XmlRpcStruct> bondingCaptor;

    @SuppressWarnings("unchecked")
    @Test
    public void bondModified() {
        List<VdsNetworkInterface> bonds = new ArrayList<VdsNetworkInterface>();
        VdsNetworkInterface bond = createVdsInterface("bond0", true, null, RandomUtils.instance().nextString(100));
        bonds.add(bond);

        int slaveCount = RandomUtils.instance().nextInt(2, 100);
        List<VdsNetworkInterface> slaves = new ArrayList<VdsNetworkInterface>(slaveCount);
        for (int i = 0; i < slaveCount; i++) {
            slaves.add(createVdsInterface("eth" + i, false, bond.getName(), null));
        }

        List<VdsNetworkInterface> ifaces = new ArrayList<VdsNetworkInterface>(slaveCount + 1);
        ifaces.add(bond);
        ifaces.addAll(slaves);

        SetupNetworksVdsCommandParameters parameters =
                new SetupNetworksVdsCommandParameters(Guid.NewGuid(),
                        Collections.<network> emptyList(),
                        Collections.<String> emptyList(),
                        bonds,
                        Collections.<VdsNetworkInterface> emptyList(),
                        ifaces);

        createCommand(parameters).Execute();

        verify(server).setupNetworks(any(XmlRpcStruct.class), bondingCaptor.capture(), any(XmlRpcStruct.class));
        XmlRpcStruct bondingStruct = bondingCaptor.getValue();
        Map<String, Object> bondStruct = (Map<String, Object>) bondingStruct.getItem(bond.getName());
        assertNotNull("Bond " + bond.getName() + " should've been sent but wasn't.", bondStruct);

        List<String> nicsInStruct = (List<String>) bondStruct.get(SetupNetworksVDSCommand.SLAVES);
        for (VdsNetworkInterface slave : slaves) {
            assertTrue("Slave " + slave.getName() + " should've been sent but wasn't.",
                    nicsInStruct.contains(slave.getName()));
        }

        assertEquals(bond.getBondOptions(), bondStruct.get(SetupNetworksVDSCommand.BONDING_OPTIONS));
    }

    private SetupNetworksVDSCommand<SetupNetworksVdsCommandParameters> createCommand(
            SetupNetworksVdsCommandParameters parameters) {
        final DbFacade dbFacade = mock(DbFacade.class);
        final VdsDAO vdsDao = mock(VdsDAO.class);

        when(dbFacade.getVdsDAO()).thenReturn(vdsDao);

        // No way to avoid these calls by regular mocking, so must implement anonymously.
        return new SetupNetworksVDSCommand<SetupNetworksVdsCommandParameters>(parameters) {

            @Override
            protected IVdsServer initializeVdsBroker(Guid vdsId) {
                return server;
            }

            @Override
            protected DbFacade getDbFacade() {
                return dbFacade;
            }
        };
    }

    private VdsNetworkInterface createVdsInterface(String name,
            Boolean bonded,
            String bondName,
            String bondOptions) {
        VdsNetworkInterface iface = new VdsNetworkInterface();
        iface.setId(Guid.NewGuid());
        iface.setName(name);
        iface.setBonded(bonded);
        iface.setBondName(bondName);
        iface.setBondOptions(bondOptions);
        return iface;
    }
}
