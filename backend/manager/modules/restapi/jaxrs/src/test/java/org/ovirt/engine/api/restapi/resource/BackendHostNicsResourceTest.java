package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.HostNic;
import org.ovirt.engine.api.resource.HostNicResource;
import org.ovirt.engine.api.restapi.types.HostNicMapper;
import org.ovirt.engine.api.restapi.types.Ipv4BootProtocolMapper;
import org.ovirt.engine.api.restapi.types.Ipv6BootProtocolMapper;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkStatistics;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendHostNicsResourceTest
    extends AbstractBackendCollectionResourceTest<HostNic, VdsNetworkInterface, BackendHostNicsResource> {

    public static final Guid PARENT_GUID = GUIDS[0];
    public static final String NETWORK_NAME = "skynet";
    public static final Ipv4BootProtocol IPV4_BOOT_PROTOCOL = Ipv4BootProtocol.STATIC_IP;
    public static final Ipv6BootProtocol IPV6_BOOT_PROTOCOL = Ipv6BootProtocol.AUTOCONF;
    public static final Guid MASTER_GUID = new Guid("99999999-9999-9999-9999-999999999999");
    public static final String MASTER_NAME = "master";
    private static final Guid SLAVE_GUID = new Guid("66666666-6666-6666-6666-666666666666");
    private static final String SLAVE_NAME = "slave";
    private static final int SINGLE_NIC_IDX = GUIDS.length - 2;
    private static final Integer NIC_SPEED = 100;
    private static final InterfaceStatus NIC_STATUS = InterfaceStatus.UP;

    public BackendHostNicsResourceTest() {
        super(new BackendHostNicsResource(PARENT_GUID.toString()), null, null);
    }

    @Test
    @Disabled
    @Override
    public void testQuery() {
    }

    @Test
    public void testGet() throws Exception {
        HostNicResource subresource = collection.getNicResource(GUIDS[SINGLE_NIC_IDX].toString());

        setGetVdsQueryExpectations(1);
        setGetNetworksQueryExpectations(1);
        setUriInfo(setUpBasicUriExpectations());
        setUpQueryExpectations("");

        verifyModel(subresource.get(), SINGLE_NIC_IDX);
    }

    @Test
    public void testListIncludeStatistics() throws Exception {
        try {
            accepts.add("application/xml; detail=statistics");
            UriInfo uriInfo = setUpBasicUriExpectations();
            setGetVdsQueryExpectations(1);
            setGetNetworksQueryExpectations(1);
            setUpQueryExpectations("");
            collection.setUriInfo(uriInfo);

            List<HostNic> nics = getCollection();
            assertTrue(nics.get(0).isSetStatistics());

            verifyCollection(nics);
        } finally {
            accepts.clear();
        }
    }

    @Override
    @Test
    public void testList() throws Exception {
        UriInfo uriInfo = setUpBasicUriExpectations();
        setGetVdsQueryExpectations(1);
        setGetNetworksQueryExpectations(1);
        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        assertEquals("", query);

        setUpEntityQueryExpectations(QueryType.GetVdsInterfacesByVdsId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { PARENT_GUID },
                                     setUpInterfaces(),
                                     failure);

    }

    public static List<VdsNetworkInterface> setUpInterfaces() {
        List<VdsNetworkInterface> ifaces = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            ifaces.add(getEntitySpecific(i));
        }
        ifaces.add(getMaster());
        ifaces.add(getSlave());
        return ifaces;
    }

    @Override
    protected VdsNetworkInterface getEntity(int index) {
        return getEntitySpecific(index);
    }

    public static VdsNetworkInterface getEntitySpecific(int index) {
        VdsNetworkInterface entity = new VdsNetworkInterface();
        entity.setId(GUIDS[index]);
        entity.setName(NAMES[index]);
        entity.setNetworkName(NETWORK_NAME);
        entity.setSpeed(NIC_SPEED);
        entity = setUpStatistics(entity, GUIDS[index]);
        entity.getStatistics().setStatus(NIC_STATUS);
        entity.setIpv4BootProtocol(IPV4_BOOT_PROTOCOL);
        entity.setIpv6BootProtocol(IPV6_BOOT_PROTOCOL);
        return entity;
    }

    public static VdsNetworkInterface getMaster() {
        VdsNetworkInterface entity = new VdsNetworkInterface();
        entity.setId(MASTER_GUID);
        entity.setName(MASTER_NAME);
        entity.setNetworkName(NETWORK_NAME);
        entity.setSpeed(NIC_SPEED);
        entity.setBonded(true);
        entity.setIpv4BootProtocol(IPV4_BOOT_PROTOCOL);
        entity.setIpv6BootProtocol(IPV6_BOOT_PROTOCOL);
        return setUpStatistics(entity, MASTER_GUID);
    }

    public static VdsNetworkInterface getSlave() {
        VdsNetworkInterface entity = new VdsNetworkInterface();
        entity.setId(SLAVE_GUID);
        entity.setName(SLAVE_NAME);
        entity.setNetworkName(NETWORK_NAME);
        entity.setSpeed(NIC_SPEED);
        entity.setBondName(MASTER_NAME);
        entity.setIpv4BootProtocol(IPV4_BOOT_PROTOCOL);
        entity.setIpv6BootProtocol(IPV6_BOOT_PROTOCOL);
        return setUpStatistics(entity, SLAVE_GUID);
    }

    public static VdsNetworkInterface setUpStatistics(VdsNetworkInterface entity, Guid id) {
        VdsNetworkStatistics statistics = new VdsNetworkStatistics();

        statistics.setId(null);
        statistics.setReceiveDrops(BigInteger.ONE);
        statistics.setReceiveRate(2D);
        statistics.setTransmitDrops(new BigInteger("3"));
        statistics.setTransmitRate(4D);
        statistics.setReceivedBytes(new BigInteger("5"));
        statistics.setTransmittedBytes(new BigInteger("6"));
        statistics.setVdsId(id);
        statistics.setStatus(null);
        entity.setStatistics(statistics);
        return entity;
    }

    @Override
    protected void verifyModel(HostNic model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    public void verifyModelSpecific(HostNic model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        assertNotNull(model.getNetwork());
        assertEquals(NETWORK_NAME, model.getNetwork().getName());
        assertEquals(calcSpeed(NIC_SPEED), model.getSpeed());
        assertNotNull(model.getStatus());
        assertEquals(HostNicMapper.mapNicStatus(NIC_STATUS), model.getStatus());
        assertEquals(Ipv4BootProtocolMapper.map(IPV4_BOOT_PROTOCOL), model.getBootProtocol());
        assertEquals(Ipv6BootProtocolMapper.map(IPV6_BOOT_PROTOCOL), model.getIpv6BootProtocol());
    }

    private Long calcSpeed(Integer nicSpeed) {
        return nicSpeed == 0 ?
                             null
                             :
                             nicSpeed * 1000L * 1000;
    }

    protected void verifyMaster(HostNic model) {
        assertEquals(MASTER_GUID.toString(), model.getId());
        assertEquals(MASTER_NAME, model.getName());
        assertNotNull(model.getNetwork());
        assertEquals(NETWORK_NAME, model.getNetwork().getName());
        assertNotNull(model.getBonding());
        assertNotNull(model.getBonding().getSlaves());
        assertEquals(1, model.getBonding().getSlaves().getHostNics().size());
        assertEquals(SLAVE_GUID.toString(), model.getBonding().getSlaves().getHostNics().get(0).getId());
        assertNotNull(model.getBonding().getSlaves().getHostNics().get(0).getHref());
    }

    protected void verifySlave(HostNic model) {
        assertEquals(SLAVE_GUID.toString(), model.getId());
        assertEquals(SLAVE_NAME, model.getName());
        assertNotNull(model.getNetwork());
        assertEquals(NETWORK_NAME, model.getNetwork().getName());
        assertEquals(5, model.getLinks().size());
        assertTrue("master".equals(model.getLinks().get(0).getRel()) ||
                   "master".equals(model.getLinks().get(1).getRel()));
        assertNotNull(model.getLinks().get(0).getHref());
    }

    @Override
    protected void verifyCollection(List<HostNic> collection) {
        assertNotNull(collection);
        assertEquals(NAMES.length + 2, collection.size());
        for (int i = 0; i < NAMES.length; i++) {
            verifyModel(collection.get(i), i);
        }
        verifyMaster(collection.get(NAMES.length));
        verifySlave(collection.get(NAMES.length + 1));
    }

    @Override
    protected List<HostNic> getCollection() {
        return collection.list().getHostNics();
    }

    protected void setGetVdsQueryExpectations(int times) {
        while (times-- > 0) {
            VDS vds = new VDS();
            vds.setClusterId(GUIDS[0]);
            setUpEntityQueryExpectations(QueryType.GetVdsByVdsId,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { PARENT_GUID },
                    vds);
        }
    }

    protected void setGetNetworksQueryExpectations(int times) {
        while (times-- > 0) {
            ArrayList<org.ovirt.engine.core.common.businessentities.network.Network> networks = new ArrayList<>();
            org.ovirt.engine.core.common.businessentities.network.Network network = new org.ovirt.engine.core.common.businessentities.network.Network();
            network.setId(GUIDS[0]);
            network.setName("orcus");
            networks.add(network);
            setUpEntityQueryExpectations(QueryType.GetAllNetworksByClusterId,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[0] },
                    networks);
        }
    }
}
