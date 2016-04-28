package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.VmDeviceType;

public abstract class BackendTemplatesBasedResourceTest<R extends Template, Q, C extends AbstractBackendCollectionResource<R, Q>>
        extends AbstractBackendCollectionResourceTest<R, Q, C> {

    protected BackendTemplatesBasedResourceTest(C collection, SearchType searchType, String prefix) {
        super(collection, searchType, prefix);
    }

    @Test
    public void testAdd() throws Exception {
        setUpAddExpectations();

        setUpCreationExpectations();

        Response response = doAdd(getRestModel(0));
        assertEquals(201, response.getStatus());
        verifyModel((R)response.getEntity(), 0);
        assertNull(((R) response.getEntity()).getCreationStatus());
    }

    protected void setUpCreationExpectations() {
        setUpCreationExpectations(VdcActionType.AddVmTemplate,
                AddVmTemplateParameters.class,
                new String[]{"Name", "Description"},
                new Object[]{NAMES[0], DESCRIPTIONS[0]},
                true,
                true,
                GUIDS[0],
                asList(GUIDS[2]),
                asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
                VdcQueryType.GetVmTemplate,
                GetVmTemplateParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[0]},
                getEntity(0));
    }

    @Test
    public void testAddCantDo() throws Exception {
        doTestBadAdd(false, true, CANT_DO);
    }

    @Test
    public void testAddFailure() throws Exception {
        doTestBadAdd(true, false, FAILURE);
    }

    protected void doTestBadAdd(boolean valid, boolean success, String detail) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.AddVmTemplate,
                AddVmTemplateParameters.class,
                new String[]{"Name", "Description"},
                new Object[]{NAMES[0], DESCRIPTIONS[0]},
                valid,
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

    protected void setUpAddExpectations() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");

        setUpGetVirtioScsiExpectations(0, 0);
        setUpGetSoundcardExpectations(0, 0);
        setUpGetRngDeviceExpectations(0, 0);
        setUpGetEntityExpectations(0);
    }

    protected void testListAllConsoleAware(boolean allContent) throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        if (allContent) {
            List<String> populates = new ArrayList<>();
            populates.add("true");
            expect(httpHeaders.getRequestHeader(BackendResource.POPULATE)).andReturn(populates).anyTimes();
            setUpGetConsoleExpectations(0, 1, 2);
            setUpGetVirtioScsiExpectations(0, 1, 2);
            setUpGetSoundcardExpectations(0, 1, 2);
            setUpGetRngDeviceExpectations(0, 1, 2);
            setUpGetBallooningExpectations(3);
        }

        setUpGetGraphicsExpectations(3);
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

    protected void setUpGetGraphicsExpectations(int times) throws Exception {
        for (int i = 0; i < times; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetGraphicsDevices,
                    IdQueryParameters.class,
                    new String[] {},
                    new Object[] {},
                    Collections.singletonList(new GraphicsDevice(VmDeviceType.SPICE)));
        }
    }

    protected abstract Response doAdd(R model);

    protected abstract R getRestModel(int index);

    protected void setUpGetBallooningExpectations(Integer... idxs) throws Exception {
        for (int i : idxs) {
            setUpGetEntityExpectations(VdcQueryType.IsBalloonEnabled,
                    IdQueryParameters.class,
                    new String[]{"Id"},
                    new Object[]{GUIDS[i]},
                    true);
        }
    }

    protected void setUpGetBallooningExpectations(int times) throws Exception {
        List<Integer> idxs = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            idxs.add(i);
        }
        setUpGetBallooningExpectations(idxs.toArray(new Integer[times]));
    }
}
