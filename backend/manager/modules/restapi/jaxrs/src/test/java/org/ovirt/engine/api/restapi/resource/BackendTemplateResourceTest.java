package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.verifyModelSpecific;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VmTemplateManagementParameters;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.VmIcon;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendTemplateResourceTest
    extends AbstractBackendSubResourceTest<Template, VmTemplate, BackendTemplateResource> {

    public BackendTemplateResourceTest() {
        super(new BackendTemplateResource(GUIDS[0].toString()));
    }

    @Test
    public void testBadGuid() {
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> new BackendTemplateResource("foo")));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetGraphicsExpectations(1);
        setUpGetEntityExpectations(1);

        verifyModel(resource.get(), 0);
    }

    protected void setUpGetEntityExpectations(int times) {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) {
        while (times-- > 0) {
            setUpGetEntityExpectations(QueryType.GetVmTemplate,
                    GetVmTemplateParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[0] },
                    notFound ? null : getEntity(0));
        }
    }

    @Test
    public void testGetWithConsoleSet() {
        testGetConsoleAware(true);
    }

    @Test
    public void testGetWithConsoleNotSet() {
        testGetConsoleAware(false);
    }

    public void testGetConsoleAware(boolean allContent) {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);

        if (allContent) {
            List<String> populates = new ArrayList<>();
            populates.add("true");
            when(httpHeaders.getRequestHeader(BackendResource.ALL_CONTENT_HEADER)).thenReturn(populates);
            setUpGetConsoleExpectations(0);
            setUpGetVirtioScsiExpectations(0);
            setUpGetSoundcardExpectations(0);
            setUpGetRngDeviceExpectations(0);
            setUpGetTpmExpectations(0);
        }
        setUpGetGraphicsExpectations(1);

        Template response = resource.get();
        verifyModel(response, 0);

        List<String> populateHeader = httpHeaders.getRequestHeader(BackendResource.ALL_CONTENT_HEADER);
        boolean populated = populateHeader != null ? populateHeader.contains("true") : false;
        assertTrue(populated ? response.isSetConsole() : !response.isSetConsole());
    }

    protected void setUpGetVirtioScsiExpectations(int ... idxs) {
        for (int i = 0; i < idxs.length; i++) {
            setUpGetEntityExpectations(QueryType.GetVirtioScsiControllers,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[idxs[i]] },
                    new ArrayList<>());
        }
    }

    protected void setUpGetSoundcardExpectations(int ... idxs) {
        for (int i = 0; i < idxs.length; i++) {
            setUpGetEntityExpectations(QueryType.GetSoundDevices,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[idxs[i]] },
                    new ArrayList<>());
        }
    }

    @Test
    public void testUpdateNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.update(getRestModel(0))));
    }

    @Test
    public void testUpdateCantDo() {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() {
        doTestBadUpdate(true, false, FAILURE);
    }

    protected void doTestBadUpdate(boolean valid, boolean success, String detail) {
        setUpGetEntityExpectations(1);
        setUriInfo(setUpActionExpectations(ActionType.UpdateVmTemplate,
                UpdateVmTemplateParameters.class,
                new String[]{},
                new Object[]{},
                valid,
                success));

        verifyFault(assertThrows(WebApplicationException.class, () -> resource.update(getRestModel(0))), detail);
    }

    @Test
    public void testConflictedUpdate() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);

        Template model = getRestModel(1);
        model.setId(GUIDS[1].toString());
        verifyImmutabilityConstraint(assertThrows(WebApplicationException.class, () -> resource.update(model)));
    }

    protected void setUpUpdateExpectations() {
        setUpGetEntityExpectations(2);
        setUpGetConsoleExpectations(0);
        setUpGetVirtioScsiExpectations(0);
        setUpGetSoundcardExpectations(0);
        setUpGetRngDeviceExpectations(0);
        setUpGetTpmExpectations(0);
    }

    protected void setUpGetGraphicsExpectations(int times) {
        for (int i = 0; i < times; i++) {
            setUpGetEntityExpectations(QueryType.GetGraphicsDevices,
                    IdQueryParameters.class,
                    new String[]{"Id"},
                    new Object[]{GUIDS[i]},
                    Collections.singletonList(new GraphicsDevice(VmDeviceType.SPICE)));
        }
    }

    private void setUpGetTpmExpectations(int ... idxs) {
        for (int i = 0; i < idxs.length; i++) {
            setUpGetEntityExpectations(QueryType.GetTpmDevices,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[idxs[i]] },
                    new ArrayList<>());
        }
    }

    @Test
    public void testUpdate() {
        setUpGetGraphicsExpectations(0);
        setUpGetGraphicsExpectations(1);
        setUpUpdateExpectations();

        setUriInfo(setUpActionExpectations(ActionType.UpdateVmTemplate,
                UpdateVmTemplateParameters.class,
                new String[]{},
                new Object[]{},
                true,
                true));

        verifyModel(resource.update(getRestModel(0)), 0);
    }

    @Test
    public void testRemove() {
        setUpGetEntityExpectations(1);
        setUpGetGraphicsExpectations(1);
        setUriInfo(setUpActionExpectations(ActionType.RemoveVmTemplate,
                VmTemplateManagementParameters.class,
                new String[] { "VmTemplateId" },
                new Object[] { GUIDS[0] },
                true,
                true));
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() {
        setUpGetEntityExpectations(QueryType.GetVmTemplate,
                GetVmTemplateParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                null);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.remove()));
    }

    @Test
    public void testRemoveCantDo() {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUpGetEntityExpectations(1);
        setUpGetGraphicsExpectations(1);
        setUriInfo(setUpActionExpectations(ActionType.RemoveVmTemplate,
                VmTemplateManagementParameters.class,
                new String[] { "VmTemplateId" },
                new Object[] { GUIDS[0] },
                valid,
                success));

        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
    }

    @Test
    public void testExport() {
        testExportWithStorageDomainId(false);
    }

    @Test
    public void testExportWithParams() {
        testExportWithStorageDomainId(true);
    }

    @Test
    public void testExportWithStorageDomainName() {
        setUpEntityQueryExpectations(QueryType.GetStorageDomainByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[2] },
                getStorageDomainStatic(2));

        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setName(NAMES[2]);

        doTestExport(storageDomain, false);
    }

    protected void testExportWithStorageDomainId(boolean exclusive) {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        doTestExport(storageDomain, exclusive);
    }

    protected void doTestExport(StorageDomain storageDomain, boolean exclusive) {
        setUriInfo(setUpActionExpectations(ActionType.ExportVmTemplate,
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
    public void testExportAsyncPending() {
        doTestExportAsync(AsyncTaskStatusEnum.init, CreationStatus.PENDING);
    }

    @Test
    public void testExportAsyncInProgress() {
        doTestExportAsync(AsyncTaskStatusEnum.running, CreationStatus.IN_PROGRESS);
    }

    @Test
    public void testExportAsyncFinished() {
        doTestExportAsync(AsyncTaskStatusEnum.finished, CreationStatus.COMPLETE);
    }

    @Test
    public void testUpdateUploadIcon() {
        setUpGetGraphicsExpectations(1);
        setUpUpdateExpectations();

        setUriInfo(setUpActionExpectations(ActionType.UpdateVmTemplate,
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
    public void testUpdateUseExistingIcons() {
        setUpGetGraphicsExpectations(1);
        setUpUpdateExpectations();

        setUriInfo(setUpActionExpectations(ActionType.UpdateVmTemplate,
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
    public void testUpdateSetAndUploadIconFailure() {
        final Template model = getRestModel(0);
        model.setSmallIcon(IconTestHelpler.createIcon(GUIDS[2]));
        model.setLargeIcon(IconTestHelpler.createIconWithData());

        verifyFault(
                assertThrows(WebApplicationException.class, () -> verifyModel(resource.update(model), 0)), BAD_REQUEST);
    }

    private void doTestExportAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus actionStatus) {
        setUriInfo(setUpActionExpectations(ActionType.ExportVmTemplate,
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
        return setUpEntityExpectations(mock(VmTemplate.class), index);
    }

    protected UriInfo setUpActionExpectations(ActionType task,
                                              Class<? extends ActionParametersBase> clz,
                                              String[] names,
                                              Object[] values) {
        return setUpActionExpectations(task, clz, names, values, true, true, null, null, true);
    }

    protected UriInfo setUpActionExpectations(ActionType task,
                                              Class<? extends ActionParametersBase> clz,
                                              String[] names,
                                              Object[] values,
                                              ArrayList<Guid> asyncTasks,
                                              ArrayList<AsyncTaskStatus> asyncStatuses) {
        String uri = "templates/" + GUIDS[0] + "/action";
        return setUpActionExpectations(task, clz, names, values, true, true, null, asyncTasks, asyncStatuses, null, null, uri, true);
    }

    private void verifyActionResponse(Response r) {
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
