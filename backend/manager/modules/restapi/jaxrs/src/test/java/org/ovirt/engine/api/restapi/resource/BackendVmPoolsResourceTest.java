package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendVmPoolsResourceTest extends
        AbstractBackendCollectionResourceTest<VmPool, org.ovirt.engine.core.common.businessentities.VmPool, BackendVmPoolsResource> {

    public BackendVmPoolsResourceTest() {
        super(new BackendVmPoolsResource(), SearchType.VmPools, "Pools : ");
    }

    @Override
    protected List<VmPool> getCollection() {
        return collection.list().getVmPools();
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.VmPool getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.VmPool entity = mock(org.ovirt.engine.core.common.businessentities.VmPool.class);
        when(entity.getVmPoolId()).thenReturn(GUIDS[index]);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getVmPoolDescription()).thenReturn(DESCRIPTIONS[index]);
        when(entity.getVmPoolType()).thenReturn(VmPoolType.AUTOMATIC);
        when(entity.getClusterId()).thenReturn(GUIDS[2]);
        return entity;
    }

    protected org.ovirt.engine.core.common.businessentities.VmTemplate getTemplateEntity() {
        VmTemplate entity = mock(VmTemplate.class);
        when(entity.getId()).thenReturn(GUIDS[1]);
        when(entity.getClusterId()).thenReturn(GUIDS[2]);
        when(entity.getName()).thenReturn(NAMES[1]);
        when(entity.getDescription()).thenReturn(DESCRIPTIONS[1]);
        when(entity.getNumOfCpus()).thenReturn(8);
        when(entity.getNumOfSockets()).thenReturn(2);
        when(entity.getDefaultDisplayType()).thenReturn(DisplayType.vga);
        when(entity.getNumOfMonitors()).thenReturn(2);
        when(entity.getVmType()).thenReturn(VmType.Server);
        return entity;
    }

    protected org.ovirt.engine.core.common.businessentities.VM getVmEntity() {
        org.ovirt.engine.core.common.businessentities.VM entity = mock(org.ovirt.engine.core.common.businessentities.VM.class);
        when(entity.getId()).thenReturn(GUIDS[0]);
        when(entity.getStaticData()).thenReturn(new VmStatic());
        return entity;
    }

    protected org.ovirt.engine.core.common.businessentities.Cluster getClusterEntity() {
        org.ovirt.engine.core.common.businessentities.Cluster entity = mock(org.ovirt.engine.core.common.businessentities.Cluster.class);
        when(entity.getId()).thenReturn(GUIDS[2]);
        return entity;
    }

    private VmPool getModel(int index) {
        VmPool model = new VmPool();
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        model.setId(GUIDS[index].toString());
        model.setCluster(new Cluster());
        model.getCluster().setId(GUIDS[2].toString());
        model.setTemplate(new Template());
        model.getTemplate().setId(GUIDS[1].toString());
        return model;
    }



    @Test
    public void add() {
        setUriInfo(setUpBasicUriExpectations());

        setUpEntityQueryExpectations(QueryType.GetClusterById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getClusterEntity());

        setUpEntityQueryExpectations(QueryType.GetVmTemplate,
                GetVmTemplateParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[1] },
                getTemplateEntity());

        setUpGetConsoleExpectations(1);

        setUpGetEntityExpectations(QueryType.GetVirtioScsiControllers,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[1] },
                new ArrayList<>());

        setUpGetEntityExpectations(QueryType.GetSoundDevices,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[1] },
                new ArrayList<>());

        setUpGetRngDeviceExpectations(0);
        addCommonAddExpectations();

        setUpGetEntityExpectations(QueryType.GetGraphicsDevices,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                new ArrayList<>());

        setUpCreationExpectations(ActionType.AddVmPool,
             VmPoolParametersBase.class,
             new String[] { "StorageDomainId" },
             new Object[] { GUIDS[0] },
             true,
             true,
             GUIDS[0],
             QueryType.GetVmPoolById,
             IdQueryParameters.class,
             new String[] { "Id" },
             new Object[] { GUIDS[0] },
             getEntity(0));

        Response response = collection.add(getModel(0));
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof VmPool);
        verifyModelTemplate((VmPool) response.getEntity());
    }

    @Test
    public void addWithName() {
        setUriInfo(setUpBasicUriExpectations());

        setUpEntityQueryExpectations(QueryType.GetClusterById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getClusterEntity());

        setUpEntityQueryExpectations(QueryType.GetVmTemplate,
                GetVmTemplateParameters.class,
                new String[] { "Name", "ClusterId" },
                new Object[] { NAMES[1], GUIDS[2] },
                getTemplateEntity());

        setUpGetConsoleExpectations(1);
        setUpGetRngDeviceExpectations(0);

        setUpGetEntityExpectations(QueryType.GetVirtioScsiControllers,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[1] },
                new ArrayList<>());

        setUpGetEntityExpectations(QueryType.GetSoundDevices,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[1] },
                new ArrayList<>());

        setUpGetEntityExpectations(QueryType.GetGraphicsDevices,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                new ArrayList<>());

        addCommonAddExpectations();

        setUpCreationExpectations(ActionType.AddVmPool,
                VmPoolParametersBase.class,
                new String[] { "StorageDomainId" },
                new Object[] { GUIDS[0] },
                true,
                true,
                GUIDS[0],
                QueryType.GetVmPoolById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                getEntity(0));

        VmPool pool = getModel(0);
        pool.setId(null);
        pool.getTemplate().setId(null);
        pool.getTemplate().setName(NAMES[1]);
        Response response = collection.add(pool);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof VmPool);
        verifyModelTemplate((VmPool) response.getEntity());
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
    @Override
    public void testQuery() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(QUERY);

        setUpQueryExpectations(QUERY);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    protected void verifyModelTemplate(VmPool model) {
        super.verifyModel(model, 0);
        verifyModelSpecific(model);
    }

    static void verifyModelSpecific(VmPool model) {
        assertNotNull(model.getCluster());
        assertEquals(GUIDS[2].toString(), model.getCluster().getId());
    }

    private void addCommonAddExpectations() {
        setUpGetEntityExpectations(QueryType.GetVmDataByPoolName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[0] },
                getVmEntity());

        setUpGetEntityExpectations(QueryType.GetVmPayload,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                new VmPayload());

        setUpGetEntityExpectations(QueryType.GetConsoleDevices,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                new ArrayList<>());

        setUpGetEntityExpectations(QueryType.GetVirtioScsiControllers,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                new ArrayList<>());

        setUpGetEntityExpectations(QueryType.GetSoundDevices,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                new ArrayList<>());

        setUpGetEntityExpectations(QueryType.GetRngDevice,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                new ArrayList<>());

        setUpGetEntityExpectations(QueryType.GetTpmDevices,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                new ArrayList<>());
    }
}
