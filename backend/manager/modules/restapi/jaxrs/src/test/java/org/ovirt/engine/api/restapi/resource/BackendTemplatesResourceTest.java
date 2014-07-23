package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.easymock.EasyMock;
import org.junit.Test;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Permissions;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.TemplateVersion;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVmByVmNameForDataCenterParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.IdsQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplatesResourceTest
    extends BackendTemplatesBasedResourceTest<Template, VmTemplate, BackendTemplatesResource> {


    private static final String VERSION_NAME = "my new version";

    public BackendTemplatesResourceTest() {
        super(new BackendTemplatesResource(), SearchType.VmTemplate, "Template : ");
    }

    @Test
    public void testAddWithClonePermissionsDontClone() throws Exception {
        doTestAddWithClonePermissions(getModel(0), false);
    }

    @Test
    public void testAddWithClonePermissionsClone() throws Exception {
        Template model = getModel(0);
        model.setPermissions(new Permissions());
        model.getPermissions().setClone(true);

        doTestAddWithClonePermissions(model, true);
    }

    private void doTestAddWithClonePermissions(Template model, boolean copy) throws Exception{
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");

        setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[1] },
                setUpVm(GUIDS[1]));
        setUpGetEntityExpectations(0);
        setUpGetConsoleExpectations(new int[]{0, 0, 0});
        setUpGetVirtioScsiExpectations(new int[]{0, 0});
        setUpGetSoundcardExpectations(new int[]{0, 0, 0});
        setUpGetRngDeviceExpectations(new int[]{0, 0});
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        setUpCreationExpectations(VdcActionType.AddVmTemplate,
                AddVmTemplateParameters.class,
                new String[] { "Name", "Description", "CopyVmPermissions" },
                new Object[] { NAMES[0], DESCRIPTIONS[0], copy },
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

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Template);
        verifyModel((Template)response.getEntity(), 0);
        assertNull(((Template)response.getEntity()).getCreationStatus());
    }

    protected org.ovirt.engine.core.common.businessentities.VDSGroup getVdsGroupEntity() {
        return new VDSGroup();
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
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());


        setUpGetConsoleExpectations(new int[]{0, 0});
        setUpGetVirtioScsiExpectations(new int[]{0});
        setUpGetSoundcardExpectations(new int[]{0, 0});
        setUpGetRngDeviceExpectations(new int[]{0});
        setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                                   IdQueryParameters.class,
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

    public void testAdd() throws Exception {
        setUpGetConsoleExpectations(new int[]{0, 0, 0});
        setUpGetSoundcardExpectations(new int[]{0});

        setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[1] },
                                   setUpVm(GUIDS[1]));

        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        super.testAdd();
    }

    @Override
    protected Response doAdd(Template model) {
        return collection.add(model);
    }

    @Override
    protected Template getRestModel(int index) {
        return getModel(index);
    }

    @Test
    public void testAddVersionNoBaseTemplateId() throws Exception {
       setUriInfo(setUpBasicUriExpectations());
       control.replay();
       Template t = getModel(2);
       t.getVersion().setBaseTemplate(null);
       try {
         collection.add(t);
         fail("Should have failed with 400 error due to a missing base template");
       }
       catch (WebApplicationException e) {
            assertNotNull(e.getResponse());
            assertEquals(400, e.getResponse().getStatus());
        }
    }

    @Test
    public void testAddVersion() throws Exception {
            setUriInfo(setUpBasicUriExpectations());
            setUpHttpHeaderExpectations("Expect", "201-created");

            setUpGetConsoleExpectations(new int[]{2, 0, 2});
            setUpGetVirtioScsiExpectations(new int[]{2, 2});
            setUpGetSoundcardExpectations(new int[]{2, 2, 0});
            setUpGetRngDeviceExpectations(new int[]{2, 2});
            setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { GUIDS[1] },
                                       setUpVm(GUIDS[1]));
            setUpGetEntityExpectations(2);
            setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[2] },
                    getVdsGroupEntity());

            setUpCreationExpectations(VdcActionType.AddVmTemplate,
                                      AddVmTemplateParameters.class,
                                      new String[] { "Name", "Description" },
                                      new Object[] { NAMES[2], DESCRIPTIONS[2] },
                                      true,
                                      true,
                                      GUIDS[2],
                                      asList(GUIDS[2]),
                                      asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
                                      VdcQueryType.GetVmTemplate,
                                      GetVmTemplateParameters.class,
                                      new String[] { "Id" },
                                      new Object[] { GUIDS[2] },
                                      getEntity(2));

            Response response = collection.add(getModel(2));
            assertEquals(201, response.getStatus());
            assertTrue(response.getEntity() instanceof Template);
            assertEquals(((Template) response.getEntity()).getVersion().getVersionName(), VERSION_NAME);
            assertEquals(((Template) response.getEntity()).getVersion().getBaseTemplate().getId(), GUIDS[1].toString());
            verifyModel((Template)response.getEntity(), 2);
    }

    @Test
    public void testAddNamedVm() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        setUpHttpHeaderExpectations("Expect", "201-created");

        setUpGetEntityExpectations(VdcQueryType.GetVmByVmNameForDataCenter,
                GetVmByVmNameForDataCenterParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[1] },
                setUpVm(GUIDS[1]));

        setUpGetEntityExpectations(0);
        setUpGetConsoleExpectations(new int[] {0, 0, 0});
        setUpGetVirtioScsiExpectations(new int[] {0, 0});
        setUpGetSoundcardExpectations(new int[] {0, 0, 0});
        setUpGetRngDeviceExpectations(new int[]{0, 0});

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
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        setUpHttpHeaderExpectations("Expect", "201-created");

        setUpGetEntityExpectations(VdcQueryType.GetVmByVmNameForDataCenter,
                                   GetVmByVmNameForDataCenterParameters.class,
                                   new String[] { "Name" },
                                   new Object[] { NAMES[1] },
                                   setUpVm(GUIDS[1]));

        setUpGetEntityExpectations(0);
        setUpGetConsoleExpectations(new int[] {0, 0, 0});
        setUpGetVirtioScsiExpectations(new int[] {0, 0});
        setUpGetSoundcardExpectations(new int[] {0, 0, 0});
        setUpGetRngDeviceExpectations(new int[] {0, 0});

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
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[1] },
                                   setUpVm(GUIDS[1]));
        setUpGetEntityExpectations(0);

        setUpGetConsoleExpectations(new int[] {0, 0, 0});
        setUpGetVirtioScsiExpectations(new int[] {0, 0});
        setUpGetSoundcardExpectations(new int[] {0, 0, 0});
        setUpGetRngDeviceExpectations(new int[]{0, 0});

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
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[1] },
                                   setUpVm(GUIDS[1]));
        setUpGetEntityExpectations(0);

        setUpGetConsoleExpectations(new int[] {0, 0, 0});
        setUpGetVirtioScsiExpectations(new int[] {0, 0});
        setUpGetSoundcardExpectations(new int[] {0, 0, 0});
        setUpGetRngDeviceExpectations(new int[]{0, 0});

        setUpGetEntityExpectations(VdcQueryType.GetVdsGroupByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[2] },
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

    protected void doTestBadAdd(boolean canDo, boolean success, String detail) throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[1] },
                                   setUpVm(GUIDS[1]));

        setUpGetConsoleExpectations(new int[] {0});
        setUpGetSoundcardExpectations(new int[] {0});

        super.doTestBadAdd(canDo, success, detail);
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

    @Override
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
        if(index == 2) {
           expect(entity.getTemplateVersionName()).andReturn(VERSION_NAME).anyTimes();
           expect(entity.getTemplateVersionNumber()).andReturn(2).anyTimes();
           expect(entity.getBaseTemplateId()).andReturn(GUIDS[1]).anyTimes();
           expect(entity.isBaseTemplate()).andReturn(false).anyTimes();
        }
        else {
            expect(entity.getTemplateVersionNumber()).andReturn(1).anyTimes();
            // same base template id as the template itself
            expect(entity.getBaseTemplateId()).andReturn(GUIDS[index]).anyTimes();
            expect(entity.isBaseTemplate()).andReturn(true).anyTimes();
        }
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
        if(index == 2) {
            populateVersion(model);
        }
        return model;
    }

    public static void populateVersion(Template t) {
        TemplateVersion templateVersion = new TemplateVersion();
        templateVersion.setVersionName(VERSION_NAME);
        templateVersion.setVersionNumber(2);
        Template base = new Template();
        base.setId(GUIDS[1].toString());
        templateVersion.setBaseTemplate(base);
        t.setVersion(templateVersion);
    }

    @Override
    protected List<Template> getCollection() {
        return collection.list().getTemplates();
    }

    @Override
    protected void verifyCollection(List<Template> collection) throws Exception {
        super.verifyCollection(collection);

        for (Template template : collection) {
            if(template.getId().equals(GUIDS[2].toString())) {
                 assertEquals(template.getVersion().getVersionName(), VERSION_NAME);
                 assertEquals(template.getVersion().getVersionNumber(), new Integer(2));
                 assertEquals(template.getVersion().getBaseTemplate().getId(), GUIDS[1].toString());
            } else {
                assertNull(template.getVersion());
            }
        }
    }

    @Override
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
        if(index == 2) {
            assertNotNull(model.getVersion());
            assertNotSame(model.getVersion().getBaseTemplate().getId(), model.getId());
        }
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        // If the query to retrieve the virtual templates succeeds, then we will run another query to add the
        // initialization information:
        if (failure == null) {
            setUpEntityQueryExpectations(
                VdcQueryType.GetVmsInit,
                IdsQueryParameters.class,
                new String[]{},
                new Object[]{},
                setUpVmInit()
            );
        }

        // Add the default expectations:
        super.setUpQueryExpectations(query, failure);
    }

    private List<VmInit> setUpVmInit() {
        List<VmInit> vmInits = new ArrayList<>(NAMES.length);
        for (int i = 0; i < NAMES.length; i++) {
            VmInit vmInit = control.createMock(VmInit.class);
            vmInits.add(vmInit);
        }
        return vmInits;
    }

}
