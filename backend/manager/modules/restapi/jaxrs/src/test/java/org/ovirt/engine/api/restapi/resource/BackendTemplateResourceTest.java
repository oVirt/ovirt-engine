package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.core.common.action.MoveVmParameters;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.VDSGroup;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVdsGroupByVdsGroupIdParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.verifyModelSpecific;

public class BackendTemplateResourceTest
    extends AbstractBackendSubResourceTest<Template, VmTemplate, BackendTemplateResource> {

    public BackendTemplateResourceTest() {
        super(new BackendTemplateResource(GUIDS[0].toString()));
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendTemplateResource("foo");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        control.replay();

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        control.replay();
        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations(2);
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                GetVdsGroupByVdsGroupIdParameters.class,
                new String[] { "VdsGroupId" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVmTemplate,
                                           UpdateVmTemplateParameters.class,
                                           new String[] {},
                                           new Object[] {},
                                           true,
                                           true));

        verifyModel(resource.update(getModel(0)), 0);
    }

    protected org.ovirt.engine.core.common.businessentities.VDSGroup getVdsGroupEntity() {
        return new VDSGroup();
    }

    @Test
    public void testUpdateCantDo() throws Exception {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() throws Exception {
        doTestBadUpdate(true, false, FAILURE);
    }

    private void doTestBadUpdate(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(1);
        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                GetVdsGroupByVdsGroupIdParameters.class,
                new String[] { "VdsGroupId" },
                new Object[] { GUIDS[2] },
                getVdsGroupEntity());

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVmTemplate,
                                           UpdateVmTemplateParameters.class,
                                           new String[] {},
                                           new Object[] {},
                                           canDo,
                                           success));

        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testConflictedUpdate() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        control.replay();

        Template model = getModel(1);
        model.setId(GUIDS[1].toString());
        try {
            resource.update(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyImmutabilityConstraint(wae);
        }
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
        setUpGetEntityExpectations("Storage: name=" + NAMES[2],
                                   SearchType.StorageDomain,
                                   getStorageDomain(2));

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
                                           new String[] { "ContainerId", "StorageDomainId", "ForceOverride" },
                                           new Object[] { GUIDS[0], GUIDS[2], exclusive }));

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

    protected void setUpGetEntityExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetVmTemplate,
                       GetVmTemplateParameters.class,
                       new String[] { "Id" },
                       new Object[] { GUIDS[0] },
                       notFound ? null : getEntity(0));
        }
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

    protected void verifyModel(Template model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model, index);
    }

    protected org.ovirt.engine.core.common.businessentities.StorageDomain getStorageDomain(int idx) {
        org.ovirt.engine.core.common.businessentities.StorageDomain dom = new org.ovirt.engine.core.common.businessentities.StorageDomain();
        dom.setId(GUIDS[idx]);
        return dom;
    }
}
