package org.ovirt.engine.api.restapi.resource;

import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.core.common.action.MoveVmParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import java.util.ArrayList;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.verifyModelSpecific;

public class BackendTemplateResourceTest
    extends BackendTemplateBasedResourceTest<Template, VmTemplate, BackendTemplateResource> {

    public BackendTemplateResourceTest() {
        super(new BackendTemplateResource(GUIDS[0].toString()));
    }

    public void testUpdate() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        super.testUpdate();
    }

    protected org.ovirt.engine.core.common.businessentities.VDSGroup getVdsGroupEntity() {
        return new VDSGroup();
    }

    protected void doTestBadUpdate(boolean canDo, boolean success, String detail) throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        super.doTestBadUpdate(canDo, success, detail);
    }

    @Test
    public void testExport() throws Exception {
        testExportWithStorageDomainId(false);
    }

    @Test
    public void testExportWithParams() throws Exception {
        testExportWithStorageDomainId(true);
    }

    @Test
    public void testExportWithStorageDomainName() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[2] },
                getStorageDomainStatic(2));

        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setName(NAMES[2]);

        doTestExport(storageDomain, false);
    }

    protected void testExportWithStorageDomainId(boolean exclusive) throws Exception {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        doTestExport(storageDomain, exclusive);
    }

    protected void doTestExport(StorageDomain storageDomain, boolean exclusive) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.ExportVmTemplate,
                MoveVmParameters.class,
                new String[]{"ContainerId", "StorageDomainId", "ForceOverride"},
                new Object[]{GUIDS[0], GUIDS[2], exclusive}));

        Action action = new Action();
        action.setStorageDomain(storageDomain);
        if (exclusive) {
            action.setExclusive(exclusive);
        }
        verifyActionResponse(resource.export(action));
    }

    @Test
    public void testIncompleteExport() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        try {
            control.replay();
            resource.export(new Action());
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Action", "export", "storageDomain.id|name");
        }
    }

    @Test
    public void testExportAsyncPending() throws Exception {
        doTestExportAsync(AsyncTaskStatusEnum.init, CreationStatus.PENDING);
    }

    @Test
    public void testExportAsyncInProgress() throws Exception {
        doTestExportAsync(AsyncTaskStatusEnum.running, CreationStatus.IN_PROGRESS);
    }

    @Test
    public void testExportAsyncFinished() throws Exception {
        doTestExportAsync(AsyncTaskStatusEnum.finished, CreationStatus.COMPLETE);
    }

    private void doTestExportAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus actionStatus) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.ExportVmTemplate,
                                           MoveVmParameters.class,
                                           new String[] { "ContainerId", "StorageDomainId", "ForceOverride" },
                                           new Object[] { GUIDS[0], GUIDS[2], false },
                                           asList(GUIDS[1]),
                                           asList(new AsyncTaskStatus(asyncStatus))));

        Action action = new Action();
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        action.setStorageDomain(storageDomain);

        Response response = resource.export(action);
        verifyActionResponse(response, "templates/" + GUIDS[0], true, null, null);
        action = (Action)response.getEntity();
        assertTrue(action.isSetStatus());
        assertEquals(actionStatus.value(), action.getStatus().getState());
    }

    @Override
    protected VmTemplate getEntity(int index) {
        return setUpEntityExpectations(control.createMock(VmTemplate.class), index);
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
                                              Class<? extends VdcActionParametersBase> clz,
                                              String[] names,
                                              Object[] values) {
        return setUpActionExpectations(task, clz, names, values, true, true, null, null, true);
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
                                              Class<? extends VdcActionParametersBase> clz,
                                              String[] names,
                                              Object[] values,
                                              ArrayList<Guid> asyncTasks,
                                              ArrayList<AsyncTaskStatus> asyncStatuses) {
        String uri = "templates/" + GUIDS[0] + "/action";
        return setUpActionExpectations(task, clz, names, values, true, true, null, asyncTasks, asyncStatuses, null, null, uri, true);
    }

    private void verifyActionResponse(Response r) throws Exception {
        verifyActionResponse(r, "templates/" + GUIDS[0], false);
    }

    @Override
    protected void verifyModel(Template model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model, index);
    }

    protected org.ovirt.engine.core.common.businessentities.StorageDomainStatic getStorageDomainStatic(int idx) {
        org.ovirt.engine.core.common.businessentities.StorageDomainStatic dom =
                new org.ovirt.engine.core.common.businessentities.StorageDomainStatic();
        dom.setId(GUIDS[idx]);
        return dom;
    }

    protected org.ovirt.engine.core.common.businessentities.StorageDomain getStorageDomain(int idx) {
        org.ovirt.engine.core.common.businessentities.StorageDomain dom = new org.ovirt.engine.core.common.businessentities.StorageDomain();
        dom.setId(GUIDS[idx]);
        return dom;
    }

    @Override
    protected Template getRestModel(int index) {
        return getModel(index);
    }
}
