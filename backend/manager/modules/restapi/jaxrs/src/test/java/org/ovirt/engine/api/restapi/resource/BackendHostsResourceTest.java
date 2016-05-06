package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostStatus;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.hostdeploy.AddVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendHostsResourceTest
        extends AbstractBackendCollectionResourceTest<Host, VDS, BackendHostsResource> {

    protected static final String[] ADDRESSES = { "10.11.12.13", "13.12.11.10", "10.01.10.01" };
    protected static final VDSStatus[] VDS_STATUS = { VDSStatus.Up, VDSStatus.Down, VDSStatus.Up };
    protected static final HostStatus[] HOST_STATUS = { HostStatus.UP, HostStatus.DOWN,
            HostStatus.UP };
    protected static final String ROOT_PASSWORD = "s3CR3t";

    public BackendHostsResourceTest() {
        super(new BackendHostsResource(), SearchType.VDS, "Hosts : ");
    }

    @Test
    @Override
    public void testQuery() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(QUERY);
        setUpQueryExpectations(QUERY);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Test
    @Override
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Test
    public void testListIncludeStatistics() throws Exception {
        try {
            accepts.add("application/xml; detail=statistics");
            UriInfo uriInfo = setUpUriExpectations(null);
            setUpQueryExpectations("");
            collection.setUriInfo(uriInfo);
            List<Host> hosts = getCollection();
            assertTrue(hosts.get(0).isSetStatistics());
            verifyCollection(hosts);
        } finally {
            accepts.clear();
        }
    }

    @Test
    public void testAddHost() throws Exception {
        setUriInfo(setUpBasicUriExpectations());

        setUpEntityQueryExpectations(VdcQueryType.GetClusterByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { "Default" },
                setUpCluster(GUIDS[1]));

        setUpCreationExpectations(VdcActionType.AddVds,
                                  AddVdsActionParameters.class,
                                  new String[] { "RootPassword" },
                                  new Object[] { ROOT_PASSWORD },
                                  true,
                                  true,
                                  GUIDS[0],
                                  VdcQueryType.GetVdsByVdsId,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));

        Host model = getModel(0);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Host);
        verifyModel((Host) response.getEntity(), 0);
    }

    @Test
    public void testAddHostClusterByName() throws Exception {
        setUriInfo(setUpBasicUriExpectations());

        setUpEntityQueryExpectations(VdcQueryType.GetClusterByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[1] },
                setUpCluster(GUIDS[1]));

        setUpCreationExpectations(VdcActionType.AddVds,
                                  AddVdsActionParameters.class,
                                  new String[] { "RootPassword" },
                                  new Object[] { ROOT_PASSWORD },
                                  true,
                                  true,
                                  GUIDS[0],
                                  VdcQueryType.GetVdsByVdsId,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));

        Host model = getModel(0);
        model.setCluster(new Cluster());
        model.getCluster().setName(NAMES[1]);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Host);
        verifyModel((Host) response.getEntity(), 0);
    }

    @Test
    public void testAddHostClusterById() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(VdcActionType.AddVds,
                                  AddVdsActionParameters.class,
                                  new String[] { "RootPassword" },
                                  new Object[] { ROOT_PASSWORD },
                                  true,
                                  true,
                                  GUIDS[0],
                                  VdcQueryType.GetVdsByVdsId,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));

        Host model = getModel(0);
        model.setCluster(new Cluster());
        model.getCluster().setId(GUIDS[1].toString());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Host);
        verifyModel((Host) response.getEntity(), 0);
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        Host model = new Host();
        model.setName(NAMES[0]);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Host", "add", "address");
        }
    }

    @Test
    public void testAddHostCantDo() throws Exception {
        doTestBadAddHost(false, true, CANT_DO);
    }

    @Test
    public void testAddHostFailure() throws Exception {
        doTestBadAddHost(true, false, FAILURE);
    }

    private void doTestBadAddHost(boolean valid, boolean success, String detail) throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetClusterByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { "Default" },
                setUpCluster(GUIDS[1]));

        setUriInfo(setUpActionExpectations(VdcActionType.AddVds,
                                           AddVdsActionParameters.class,
                                           new String[] { "RootPassword" },
                                           new Object[] { ROOT_PASSWORD },
                                           valid,
                                           success));
        Host model = getModel(0);

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Override
    protected VDS getEntity(int index) {
        return setUpEntityExpectations(control.createMock(VDS.class),
                                       control.createMock(VdsStatistics.class),
                                       index);
    }

    static VDS setUpEntityExpectations(VDS entity, int index) {
        return setUpEntityExpectations(entity, null, index);
    }

    static VDS setUpEntityExpectations(VDS entity, VdsStatistics statistics, int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getHostName()).andReturn(ADDRESSES[index]).anyTimes();
        expect(entity.getStatus()).andReturn(VDS_STATUS[index]).anyTimes();
        expect(entity.getStoragePoolId()).andReturn(GUIDS[1]).anyTimes();
        VdsStatic vdsStatic = new VdsStatic();
        vdsStatic.setId(GUIDS[2]);
        expect(entity.getStaticData()).andReturn(vdsStatic).anyTimes();
        if (statistics != null) {
            setUpStatisticalEntityExpectations(entity, statistics);
        }
        return entity;
    }

    static VDS setUpStatisticalEntityExpectations(VDS entity, VdsStatistics statistics) {
        expect(entity.getPhysicalMemMb()).andReturn(5120).anyTimes();
        expect(entity.getStatisticsData()).andReturn(statistics).anyTimes();
        expect(statistics.getUsageMemPercent()).andReturn(20).anyTimes();
        expect(statistics.getSwapFree()).andReturn(25L).anyTimes();
        expect(statistics.getSwapTotal()).andReturn(30L).anyTimes();
        expect(statistics.getMemAvailable()).andReturn(35L).anyTimes();
        expect(statistics.getMemShared()).andReturn(38L).anyTimes();
        expect(statistics.getKsmCpuPercent()).andReturn(40).anyTimes();
        expect(statistics.getCpuUser()).andReturn(45.0).anyTimes();
        expect(statistics.getCpuSys()).andReturn(50.0).anyTimes();
        expect(statistics.getCpuIdle()).andReturn(55.0).anyTimes();
        expect(statistics.getCpuLoad()).andReturn(0.60).anyTimes();
        return entity;
    }

    @Override
    protected List<Host> getCollection() {
        return collection.list().getHosts();
    }

    static Host getModel(int index) {
        Host model = new Host();
        model.setName(NAMES[index]);
        model.setAddress(ADDRESSES[index]);
        model.setRootPassword(ROOT_PASSWORD);
        return model;
    }

    @Override
    protected void verifyModel(Host model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    static void verifyModelSpecific(Host model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        assertEquals(ADDRESSES[index], model.getAddress());
        assertEquals(HOST_STATUS[index], model.getStatus());
    }
}
