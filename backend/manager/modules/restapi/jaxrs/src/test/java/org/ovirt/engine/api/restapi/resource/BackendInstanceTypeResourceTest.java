package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.resource.BackendInstanceTypesResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendInstanceTypesResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendInstanceTypesResourceTest.verifyModelSpecific;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VmTemplateManagementParameters;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.VmDeviceType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendInstanceTypeResourceTest
    extends AbstractBackendSubResourceTest<InstanceType, org.ovirt.engine.core.common.businessentities.InstanceType, BackendInstanceTypeResource> {

    public BackendInstanceTypeResourceTest() {
        super(new BackendInstanceTypeResource(GUIDS[0].toString()));
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

        InstanceType response = resource.get();
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
    public void testUpdate() {
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

        InstanceType model = getRestModel(1);
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

    @Test
    public void testRemove() {
        setUpGetGraphicsExpectations(1);
        setUpGetEntityExpectations(1);
        setUriInfo(setUpActionExpectations(
                ActionType.RemoveVmTemplate,
                VmTemplateManagementParameters.class,
                new String[] { "VmTemplateId" },
                new Object[] { GUIDS[0] },
                true,
                true));
        Response response = resource.remove();
        verifyRemove(response);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.InstanceType getEntity(int index) {
        return setUpEntityExpectations(mock(VmTemplate.class), index);
    }

    private InstanceType getRestModel(int index) {
        return getModel(index);
    }

    @Override
    protected void verifyModel(InstanceType model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model);
    }

    private void setUpGetEntityExpectations(int times, boolean notFound) {
        while (times-- > 0) {
            setUpGetEntityExpectations(QueryType.GetInstanceType,
                    GetVmTemplateParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[0] },
                    notFound ? null : getEntity(0));
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
}
