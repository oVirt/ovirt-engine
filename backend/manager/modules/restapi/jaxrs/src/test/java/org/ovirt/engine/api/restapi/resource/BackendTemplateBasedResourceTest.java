package org.ovirt.engine.api.restapi.resource;

import org.junit.Test;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.UpdatableResource;
import org.ovirt.engine.api.restapi.util.VmHelper;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.expect;

public abstract class BackendTemplateBasedResourceTest<
        R extends Template,
        Q,
        S extends AbstractBackendSubResource<R, Q> & UpdatableResource<R>>
        extends AbstractBackendSubResourceTest<R, Q, S> {
    protected VmHelper vmHelper = VmHelper.getInstance();

    protected BackendTemplateBasedResourceTest(S resource) {
        super(resource);
    }

    @Override
    protected void init() {
        super.init();
        initBackendResource(vmHelper);
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

        if (allContent) {
            List<String> populates = new ArrayList<String>();
            populates.add("true");
            expect(httpHeaders.getRequestHeader(BackendResource.POPULATE)).andReturn(populates).anyTimes();
            setUpGetConsoleExpectations(new int[]{0});
            setUpGetVirtioScsiExpectations(new int[]{0});
            setUpGetSoundcardExpectations(new int[]{0});
            setUpGetRngDeviceExpectations(new int [] {0});
        }
        control.replay();

        R response = resource.get();
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
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations(2);
        setUpGetConsoleExpectations(new int[]{0});
        setUpGetVirtioScsiExpectations(new int[]{0});
        setUpGetSoundcardExpectations(new int[]{0});
        setUpGetRngDeviceExpectations(new int[]{0});

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVmTemplate,
                UpdateVmTemplateParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true));

        verifyModel(resource.update(getRestModel(0)), 0);
    }

    @Test
    public void testUpdateCantDo() throws Exception {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() throws Exception {
        doTestBadUpdate(true, false, FAILURE);
    }

    protected void doTestBadUpdate(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(1);
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVmTemplate,
                UpdateVmTemplateParameters.class,
                new String[] {},
                new Object[] {},
                canDo,
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

        R model = getRestModel(1);
        model.setId(GUIDS[1].toString());
        try {
            resource.update(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyImmutabilityConstraint(wae);
        }
    }

    protected abstract R getRestModel(int index);
}
