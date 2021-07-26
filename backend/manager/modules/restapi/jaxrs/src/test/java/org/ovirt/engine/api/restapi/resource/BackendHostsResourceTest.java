package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostStatus;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.hostdeploy.AddVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendHostsResourceTest
        extends AbstractBackendCollectionResourceTest<Host, VDS, BackendHostsResource> {

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.OrganizationName, "oVirt"));
    }

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
    public void testAddHost() {
        setUriInfo(setUpBasicUriExpectations());

        setUpEntityQueryExpectations(QueryType.GetClusterByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { "Default" },
                setUpCluster(GUIDS[1]));

        setUpCreationExpectations(ActionType.AddVds,
                                  AddVdsActionParameters.class,
                                  new String[] { "RootPassword" },
                                  new Object[] { ROOT_PASSWORD },
                                  true,
                                  true,
                                  GUIDS[0],
                                  QueryType.GetVdsByVdsId,
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
    public void testAddHostClusterByName() {
        setUriInfo(setUpBasicUriExpectations());

        setUpEntityQueryExpectations(QueryType.GetClusterByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[1] },
                setUpCluster(GUIDS[1]));

        setUpCreationExpectations(ActionType.AddVds,
                                  AddVdsActionParameters.class,
                                  new String[] { "RootPassword" },
                                  new Object[] { ROOT_PASSWORD },
                                  true,
                                  true,
                                  GUIDS[0],
                                  QueryType.GetVdsByVdsId,
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
    public void testAddHostClusterById() {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(ActionType.AddVds,
                                  AddVdsActionParameters.class,
                                  new String[] { "RootPassword" },
                                  new Object[] { ROOT_PASSWORD },
                                  true,
                                  true,
                                  GUIDS[0],
                                  QueryType.GetVdsByVdsId,
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
    public void testAddIncompleteParameters() {
        Host model = new Host();
        model.setName(NAMES[0]);
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)), "Host", "add", "address");
    }

    @Test
    public void testAddHostCantDo() {
        doTestBadAddHost(false, true, CANT_DO);
    }

    @Test
    public void testAddHostFailure() {
        doTestBadAddHost(true, false, FAILURE);
    }

    private void doTestBadAddHost(boolean valid, boolean success, String detail) {
        setUpEntityQueryExpectations(QueryType.GetClusterByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { "Default" },
                setUpCluster(GUIDS[1]));

        setUriInfo(setUpActionExpectations(ActionType.AddVds,
                                           AddVdsActionParameters.class,
                                           new String[] { "RootPassword" },
                                           new Object[] { ROOT_PASSWORD },
                                           valid,
                                           success));
        Host model = getModel(0);

        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(model)), detail);
    }

    @Override
    protected VDS getEntity(int index) {
        return setUpEntityExpectations(spy(new VDS()),
                                       mock(VdsStatistics.class),
                                       index);
    }

    static VDS setUpEntityExpectations(VDS entity, int index) {
        return setUpEntityExpectations(entity, null, index);
    }

    static VDS setUpEntityExpectations(VDS entity, VdsStatistics statistics, int index) {
        entity.setId(GUIDS[index]);
        entity.getStaticData().setName(NAMES[index]);
        entity.setHostName(ADDRESSES[index]);
        entity.setStatus(VDS_STATUS[index]);
        entity.setStoragePoolId(GUIDS[1]);

        if (statistics != null) {
            setUpStatisticalEntityExpectations(entity, statistics);
        }
        return entity;
    }

    static VDS setUpStatisticalEntityExpectations(VDS entity, VdsStatistics statistics) {
        when(entity.getPhysicalMemMb()).thenReturn(5120);
        when(entity.getStatisticsData()).thenReturn(statistics);
        when(statistics.getUsageMemPercent()).thenReturn(20);
        when(statistics.getSwapFree()).thenReturn(25L);
        when(statistics.getSwapTotal()).thenReturn(30L);
        when(statistics.getMemShared()).thenReturn(38L);
        when(statistics.getKsmCpuPercent()).thenReturn(40);
        when(statistics.getCpuUser()).thenReturn(45.0);
        when(statistics.getCpuSys()).thenReturn(50.0);
        when(statistics.getCpuIdle()).thenReturn(55.0);
        when(statistics.getCpuLoad()).thenReturn(0.60);
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
