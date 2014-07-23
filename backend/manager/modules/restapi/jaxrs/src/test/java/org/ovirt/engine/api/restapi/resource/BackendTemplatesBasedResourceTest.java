package org.ovirt.engine.api.restapi.resource;


import org.junit.Test;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.restapi.util.VmHelper;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.expect;

public abstract class BackendTemplatesBasedResourceTest<R extends Template, Q, C extends AbstractBackendCollectionResource<R, Q>>
        extends AbstractBackendCollectionResourceTest<R, Q, C> {

    protected VmHelper vmHelper = VmHelper.getInstance();

    protected BackendTemplatesBasedResourceTest(C collection, SearchType searchType, String prefix) {
        super(collection, searchType, prefix);
    }

    @Override
    public void init() {
        super.init();
        initBackendResource(vmHelper);
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations(0);
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

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(0);
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
    public void testAdd() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");

        setUpGetVirtioScsiExpectations(new int[]{0, 0});
        setUpGetSoundcardExpectations(new int[]{0, 0});
        setUpGetRngDeviceExpectations(new int[]{0, 0});
        setUpGetEntityExpectations(0);

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

        Response response = doAdd(getRestModel(0));
        assertEquals(201, response.getStatus());
        verifyModel((R)response.getEntity(), 0);
        assertNull(((R)response.getEntity()).getCreationStatus());
    }

    @Test
    public void testAddCantDo() throws Exception {
        doTestBadAdd(false, true, CANT_DO);
    }

    @Test
    public void testAddFailure() throws Exception {
        doTestBadAdd(true, false, FAILURE);
    }

    protected void doTestBadAdd(boolean canDo, boolean success, String detail) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.AddVmTemplate,
                AddVmTemplateParameters.class,
                new String[] { "Name", "Description" },
                new Object[] { NAMES[0], DESCRIPTIONS[0] },
                canDo,
                success));
        try {
            doAdd(getRestModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testListAllContentIsConsolePopulated() throws Exception {
        testListAllConsoleAware(true);
    }

    @Test
    public void testListAllContentIsNotConsolePopulated() throws Exception {
        testListAllConsoleAware(false);
    }

    protected void testListAllConsoleAware(boolean allContent) throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        if (allContent) {
            List<String> populates = new ArrayList<String>();
            populates.add("true");
            expect(httpHeaders.getRequestHeader(BackendResource.POPULATE)).andReturn(populates).anyTimes();
            setUpGetConsoleExpectations(new int[]{0, 1, 2});
            setUpGetVirtioScsiExpectations(new int[] {0, 1, 2});
            setUpGetSoundcardExpectations(new int[] {0, 1, 2});
            setUpGetRngDeviceExpectations(new int[] {0, 1, 2});
        }

        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    protected void verifyCollection(List<R> collection) throws Exception {
        super.verifyCollection(collection);

        List<String> populateHeader = httpHeaders.getRequestHeader(BackendResource.POPULATE);
        boolean populated = populateHeader != null ? populateHeader.contains("true") : false;

        for (R template : collection) {
            assertTrue(populated ? template.isSetConsole() : !template.isSetConsole());
        }
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

    protected void setUpGetEntityExpectations(int index) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetVmTemplate,
                GetVmTemplateParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[index] },
                getEntity(index));
    }

    protected abstract Response doAdd(R model);

    protected abstract R getRestModel(int index);
}
