package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.easymock.EasyMock;

import org.junit.Test;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVdsGroupByVdsGroupIdParameters;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.easymock.classextension.EasyMock.expect;
import org.ovirt.engine.core.common.queries.GetVmByVmNameForDataCenterParameters;

public class BackendTemplatesResourceTest
    extends AbstractBackendCollectionResourceTest<Template, VmTemplate, BackendTemplatesResource> {

    public BackendTemplatesResourceTest() {
        super(new BackendTemplatesResource(), SearchType.VmTemplate, "Template : ");
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveVmTemplate,
                                           VmTemplateParametersBase.class,
                                           new String[] { "VmTemplateId" },
                                           new Object[] { GUIDS[0] },
                                           true,
                                           true));
        verifyRemove(collection.remove(GUIDS[0].toString()));
    }

    @Test
    public void testRemoveNonExistant() throws Exception{
        setUpGetEntityExpectations(VdcQueryType.GetVmTemplate,
                GetVmTemplateParameters.class,
                new String[] { "Id" },
                new Object[] { NON_EXISTANT_GUID },
                null);
        control.replay();
        try {
            collection.remove(NON_EXISTANT_GUID.toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    private void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetVmTemplate,
                GetVmTemplateParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                getEntity(0));
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    protected org.ovirt.engine.core.common.businessentities.VDSGroup getVdsGroupEntity() {
        return new VDSGroup();
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveVmTemplate,
                                           VmTemplateParametersBase.class,
                                           new String[] { "VmTemplateId" },
                                           new Object[] { GUIDS[0] },
                                           canDo,
                                           success));
        try {
            collection.remove(GUIDS[0].toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddAsyncPending() throws Exception {
        doTestAddAsync(AsyncTaskStatusEnum.init, CreationStatus.PENDING);
    }

    @Test
    public void testAddAsyncInProgress() throws Exception {
        doTestAddAsync(AsyncTaskStatusEnum.running, CreationStatus.IN_PROGRESS);
    }

    @Test
    public void testAddAsyncFinished() throws Exception {
        doTestAddAsync(AsyncTaskStatusEnum.finished, CreationStatus.COMPLETE);
    }

    private void doTestAddAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus creationStatus) throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                GetVdsGroupByVdsGroupIdParameters.class,
                new String[] { "VdsGroupId" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                                   GetVmByVmIdParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[1] },
                                   setUpVm(GUIDS[1]));

        setUpCreationExpectations(VdcActionType.AddVmTemplate,
                                  AddVmTemplateParameters.class,
                                  new String[] { "Name", "Description" },
                                  new Object[] { NAMES[0], DESCRIPTIONS[0] },
                                  true,
                                  true,
                                  GUIDS[0],
                                  asList(GUIDS[2]),
                                  asList(new AsyncTaskStatus(asyncStatus)),
                                  VdcQueryType.GetVmTemplate,
                                  GetVmTemplateParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));

        Response response = collection.add(getModel(0));
        assertEquals(202, response.getStatus());
        assertTrue(response.getEntity() instanceof Template);
        verifyModel((Template) response.getEntity(), 0);
        Template created = (Template)response.getEntity();
        assertNotNull(created.getCreationStatus());
        assertEquals(creationStatus.value(), created.getCreationStatus().getState());
    }

    @Test
    public void testAdd() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");

        setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                                   GetVmByVmIdParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[1] },
                                   setUpVm(GUIDS[1]));
        setUpGetEntityExpectations();
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                GetVdsGroupByVdsGroupIdParameters.class,
                new String[] { "VdsGroupId" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        setUpCreationExpectations(VdcActionType.AddVmTemplate,
                                  AddVmTemplateParameters.class,
                                  new String[] { "Name", "Description" },
                                  new Object[] { NAMES[0], DESCRIPTIONS[0] },
                                  true,
                                  true,
                                  GUIDS[0],
                                  asList(GUIDS[2]),
                                  asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
                                  VdcQueryType.GetVmTemplate,
                                  GetVmTemplateParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));

        Response response = collection.add(getModel(0));
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Template);
        verifyModel((Template)response.getEntity(), 0);
        assertNull(((Template)response.getEntity()).getCreationStatus());
    }

    @Test
    public void testAddNamedVm() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                GetVdsGroupByVdsGroupIdParameters.class,
                new String[] { "VdsGroupId" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        setUpHttpHeaderExpectations("Expect", "201-created");

        setUpGetEntityExpectations("VM: name=" + NAMES[1],
                                   SearchType.VM,
                                   setUpVm(GUIDS[1]));
        setUpGetEntityExpectations();

        setUpCreationExpectations(VdcActionType.AddVmTemplate,
                                  AddVmTemplateParameters.class,
                                  new String[] { "Name", "Description" },
                                  new Object[] { NAMES[0], DESCRIPTIONS[0] },
                                  true,
                                  true,
                                  GUIDS[0],
                                  asList(GUIDS[2]),
                                  asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
                                  VdcQueryType.GetVmTemplate,
                                  GetVmTemplateParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));

        Template model = getModel(0);
        model.getVm().setId(null);
        model.getVm().setName(NAMES[1]);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Template);
        verifyModel((Template)response.getEntity(), 0);
        assertNull(((Template)response.getEntity()).getCreationStatus());
    }

    @Test
    public void testAddNamedVmFiltered() throws Exception {
        setUpFilteredQueryExpectations();
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                GetVdsGroupByVdsGroupIdParameters.class,
                new String[] { "VdsGroupId" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        setUpHttpHeaderExpectations("Expect", "201-created");

        setUpGetEntityExpectations(VdcQueryType.GetVmByVmNameForDataCenter,
                                   GetVmByVmNameForDataCenterParameters.class,
                                   new String[] { "Name" },
                                   new Object[] { NAMES[1] },
                                   setUpVm(GUIDS[1]));

        setUpGetEntityExpectations();

        setUpCreationExpectations(VdcActionType.AddVmTemplate,
                                  AddVmTemplateParameters.class,
                                  new String[] { "Name", "Description" },
                                  new Object[] { NAMES[0], DESCRIPTIONS[0] },
                                  true,
                                  true,
                                  GUIDS[0],
                                  asList(GUIDS[2]),
                                  asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
                                  VdcQueryType.GetVmTemplate,
                                  GetVmTemplateParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));

        Template model = getModel(0);
        model.getVm().setId(null);
        model.getVm().setName(NAMES[1]);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Template);
        verifyModel((Template)response.getEntity(), 0);
        assertNull(((Template)response.getEntity()).getCreationStatus());
    }

    @Test
    public void testAddWithCluster() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                GetVdsGroupByVdsGroupIdParameters.class,
                new String[] { "VdsGroupId" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                                   GetVmByVmIdParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[1] },
                                   setUpVm(GUIDS[1]));
        setUpGetEntityExpectations();

        setUpCreationExpectations(VdcActionType.AddVmTemplate,
                                  AddVmTemplateParameters.class,
                                  new String[] { "Name", "Description" },
                                  new Object[] { NAMES[0], DESCRIPTIONS[0] },
                                  true,
                                  true,
                                  GUIDS[0],
                                  asList(GUIDS[2]),
                                  asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
                                  VdcQueryType.GetVmTemplate,
                                  GetVmTemplateParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));

        Template model = getModel(0);
        model.setCluster(new Cluster());
        model.getCluster().setId(GUIDS[2].toString());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Template);
        verifyModel((Template)response.getEntity(), 0);
        assertNull(((Template)response.getEntity()).getCreationStatus());
    }

    @Test
    public void testAddWithClusterName() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                GetVdsGroupByVdsGroupIdParameters.class,
                new String[] { "VdsGroupId" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                                   GetVmByVmIdParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[1] },
                                   setUpVm(GUIDS[1]));
        setUpGetEntityExpectations();

        setUpGetEntityExpectations("Cluster: name=" + NAMES[2],
                                   SearchType.Cluster,
                                   setUpVDSGroup(GUIDS[2]));

        setUpCreationExpectations(VdcActionType.AddVmTemplate,
                                  AddVmTemplateParameters.class,
                                  new String[] { "Name", "Description" },
                                  new Object[] { NAMES[0], DESCRIPTIONS[0] },
                                  true,
                                  true,
                                  GUIDS[0],
                                  asList(GUIDS[2]),
                                  asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
                                  VdcQueryType.GetVmTemplate,
                                  GetVmTemplateParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));

        Template model = getModel(0);
        model.setCluster(new Cluster());
        model.getCluster().setName(NAMES[2]);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Template);
        verifyModel((Template)response.getEntity(), 0);
        assertNull(((Template)response.getEntity()).getCreationStatus());
    }

    @Test
    public void testAddCantDo() throws Exception {
        doTestBadAdd(false, true, CANT_DO);
    }

    @Test
    public void testAddFailure() throws Exception {
        doTestBadAdd(true, false, FAILURE);
    }

    protected void setUpFilteredQueryExpectations() {
        List<String> filterValue = new ArrayList<String>();
        filterValue.add("true");
        EasyMock.reset(httpHeaders);
        expect(httpHeaders.getRequestHeader(USER_FILTER_HEADER)).andReturn(filterValue);
    }

    private void doTestBadAdd(boolean canDo, boolean success, String detail) throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                GetVdsGroupByVdsGroupIdParameters.class,
                new String[] { "VdsGroupId" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                                   GetVmByVmIdParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[1] },
                                   setUpVm(GUIDS[1]));

        setUriInfo(setUpActionExpectations(VdcActionType.AddVmTemplate,
                                           AddVmTemplateParameters.class,
                                           new String[] { "Name", "Description" },
                                           new Object[] { NAMES[0], DESCRIPTIONS[0] },
                                           canDo,
                                           success));

        try {
            collection.add(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        Template model = new Template();
        model.setName(NAMES[0]);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Template", "add", "vm.id|name");
        }
    }

    protected org.ovirt.engine.core.common.businessentities.VM setUpVm(Guid id) {
        org.ovirt.engine.core.common.businessentities.VM vm =
            control.createMock(org.ovirt.engine.core.common.businessentities.VM.class);
        expect(vm.getId()).andReturn(id).anyTimes();
        return vm;
    }

    protected VmTemplate getEntity(int index) {
        return setUpEntityExpectations(control.createMock(VmTemplate.class), index);
    }

    static VmTemplate setUpEntityExpectations(VmTemplate entity, int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getVdsGroupId()).andReturn(GUIDS[2]).anyTimes();
        expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getDescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        expect(entity.getNumOfCpus()).andReturn(8).anyTimes();
        expect(entity.getNumOfSockets()).andReturn(2).anyTimes();
        return entity;
    }

    static Template getModel(int index) {
        Template model = new Template();
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        model.setVm(new VM());
        model.getVm().setId(GUIDS[1].toString());
        model.setCluster(new Cluster());
        model.getCluster().setId(GUIDS[2].toString());
        return model;
    }

    protected List<Template> getCollection() {
        return collection.list().getTemplates();
    }

    protected void verifyModel(Template model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model, index);
    }

    static void verifyModelSpecific(Template model, int index) {
        assertNotNull(model.getCluster());
        assertEquals(GUIDS[2].toString(), model.getCluster().getId());
        assertNotNull(model.getCpu());
        assertNotNull(model.getCpu().getTopology());
        assertEquals(4, model.getCpu().getTopology().getCores().intValue());
        assertEquals(2, model.getCpu().getTopology().getSockets().intValue());
    }
}
