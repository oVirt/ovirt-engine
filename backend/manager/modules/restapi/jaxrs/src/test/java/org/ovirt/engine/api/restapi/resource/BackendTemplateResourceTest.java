package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.verifyModelSpecific;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.VmIcon;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;

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
        }
        catch (WebApplicationException wae) {
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
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetGraphicsExpectations(1);
        setUpGetEntityExpectations(1);
        setUpGetBallooningExpectations();
        control.replay();

        verifyModel(resource.get(), 0);
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

    @Test
    public void testGetWithConsoleSet() throws Exception {
        testGetConsoleAware(true);
    }

    @Test
    public void testGetWithConsoleNotSet() throws Exception {
        testGetConsoleAware(false);
    }

    public void testGetConsoleAware(boolean allContent) throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        setUpGetBallooningExpectations();

        if (allContent) {
            List<String> populates = new ArrayList<>();
            populates.add("true");
            expect(httpHeaders.getRequestHeader(BackendResource.POPULATE)).andReturn(populates).anyTimes();
            setUpGetConsoleExpectations(0);
            setUpGetVirtioScsiExpectations(0);
            setUpGetSoundcardExpectations(0);
            setUpGetRngDeviceExpectations(0);
        }
        setUpGetGraphicsExpectations(1);
        control.replay();

        Template response = resource.get();
        verifyModel(response, 0);

        List<String> populateHeader = httpHeaders.getRequestHeader(BackendResource.POPULATE);
        boolean populated = populateHeader != null ? populateHeader.contains("true") : false;
        assertTrue(populated ? response.isSetConsole() : !response.isSetConsole());
    }

    protected void setUpGetVirtioScsiExpectations(int ... idxs) throws Exception {
        for (int i = 0; i < idxs.length; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetVirtioScsiControllers,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[idxs[i]] },
                    new ArrayList<>());
        }
    }

    protected void setUpGetSoundcardExpectations(int ... idxs) throws Exception {
        for (int i = 0; i < idxs.length; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetSoundDevices,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[idxs[i]] },
                    new ArrayList<>());
        }
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        control.replay();
        try {
            resource.update(getRestModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdateCantDo() throws Exception {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() throws Exception {
        doTestBadUpdate(true, false, FAILURE);
    }

    protected void doTestBadUpdate(boolean valid, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(1);
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVmTemplate,
                UpdateVmTemplateParameters.class,
                new String[]{},
                new Object[]{},
                valid,
                success));

        try {
            resource.update(getRestModel(0));
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

        Template model = getRestModel(1);
        model.setId(GUIDS[1].toString());
        try {
            resource.update(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyImmutabilityConstraint(wae);
        }
    }

    protected void setUpUpdateExpectations() throws Exception {
        setUpGetEntityExpectations(2);
        setUpGetConsoleExpectations(0);
        setUpGetVirtioScsiExpectations(0);
        setUpGetSoundcardExpectations(0);
        setUpGetRngDeviceExpectations(0);
    }

    protected void setUpGetGraphicsExpectations(int times) throws Exception {
        for (int i = 0; i < times; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetGraphicsDevices,
                    IdQueryParameters.class,
                    new String[]{"Id"},
                    new Object[]{GUIDS[i]},
                    Collections.singletonList(new GraphicsDevice(VmDeviceType.SPICE)));
        }
    }

    protected void setUpGetBallooningExpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.IsBalloonEnabled,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                true);
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetGraphicsExpectations(0);
        setUpGetGraphicsExpectations(1);
        setUpUpdateExpectations();
        setUpGetBallooningExpectations();

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVmTemplate,
                UpdateVmTemplateParameters.class,
                new String[]{},
                new Object[]{},
                true,
                true));

        verifyModel(resource.update(getRestModel(0)), 0);
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations(1);
        setUpGetGraphicsExpectations(1);
        setUpGetBallooningExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveVmTemplate,
                VmTemplateParametersBase.class,
                new String[] { "VmTemplateId" },
                new Object[] { GUIDS[0] },
                true,
                true));
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetVmTemplate,
                GetVmTemplateParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                null);
        control.replay();
        try {
            resource.remove();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(1);
        setUpGetGraphicsExpectations(1);
        setUpGetBallooningExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveVmTemplate,
                VmTemplateParametersBase.class,
                new String[] { "VmTemplateId" },
                new Object[] { GUIDS[0] },
                valid,
                success));
        try {
            resource.remove();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
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
                MoveOrCopyParameters.class,
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

    @Test
    public void testUpdateUploadIcon() throws Exception {
        setUpGetGraphicsExpectations(1);
        setUpGetBallooningExpectations();
        setUpUpdateExpectations();

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVmTemplate,
                UpdateVmTemplateParameters.class,
                new String[] { "VmLargeIcon" },
                new Object[] { VmIcon.typeAndDataToDataUrl(IconTestHelpler.MEDIA_TYPE, IconTestHelpler.DATA_URL) },
                true,
                true));

        final Template model = getRestModel(0);
        model.setLargeIcon(IconTestHelpler.createIconWithData());
        verifyModel(resource.update(model), 0);
    }

    @Test
    public void testUpdateUseExistingIcons() throws Exception {
        setUpGetGraphicsExpectations(1);
        setUpUpdateExpectations();
        setUpGetBallooningExpectations();

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVmTemplate,
                UpdateVmTemplateParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true));

        final Template model = getRestModel(0);
        model.setSmallIcon(IconTestHelpler.createIcon(GUIDS[2]));
        model.setLargeIcon(IconTestHelpler.createIcon(GUIDS[3]));
        verifyModel(resource.update(model), 0);
    }

    @Test
    public void testUpdateSetAndUploadIconFailure() throws Exception {
        control.replay();
        final Template model = getRestModel(0);
        model.setSmallIcon(IconTestHelpler.createIcon(GUIDS[2]));
        model.setLargeIcon(IconTestHelpler.createIconWithData());
        try {
            verifyModel(resource.update(model), 0);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BAD_REQUEST);
        }
    }

    private void doTestExportAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus actionStatus) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.ExportVmTemplate,
                                           MoveOrCopyParameters.class,
                                           new String[] { "ContainerId", "StorageDomainId", "ForceOverride" },
                                           new Object[] { GUIDS[0], GUIDS[2], false },
                                           asList(GUIDS[1]),
                                           asList(new AsyncTaskStatus(asyncStatus))));

        Action action = new Action();
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        action.setStorageDomain(storageDomain);

        Response response = resource.export(action);
        verifyActionResponse(response, "templates/" + GUIDS[0], true, null);
        action = (Action)response.getEntity();
        assertTrue(action.isSetStatus());
        assertEquals(actionStatus.value(), action.getStatus());
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

    private org.ovirt.engine.core.common.businessentities.StorageDomainStatic getStorageDomainStatic(int idx) {
        org.ovirt.engine.core.common.businessentities.StorageDomainStatic dom =
                new org.ovirt.engine.core.common.businessentities.StorageDomainStatic();
        dom.setId(GUIDS[idx]);
        return dom;
    }

    private Template getRestModel(int index) {
        return getModel(index);
    }
}
