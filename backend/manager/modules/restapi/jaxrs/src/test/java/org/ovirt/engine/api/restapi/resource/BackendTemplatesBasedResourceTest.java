package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.VmDeviceType;

@MockitoSettings(strictness = Strictness.LENIENT)
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
        setUpCreationExpectations(ActionType.AddVmTemplate,
                AddVmTemplateParameters.class,
                new String[]{"Name", "Description"},
                new Object[]{NAMES[0], DESCRIPTIONS[0]},
                true,
                true,
                GUIDS[0],
                asList(GUIDS[2]),
                asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
                QueryType.GetVmTemplate,
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
        setUriInfo(setUpActionExpectations(ActionType.AddVmTemplate,
                AddVmTemplateParameters.class,
                new String[]{"Name", "Description"},
                new Object[]{NAMES[0], DESCRIPTIONS[0]},
                valid,
                success));

        verifyFault(assertThrows(WebApplicationException.class, () -> doAdd(getRestModel(0))), detail);
    }

    @Test
    public void testListAllContentIsConsolePopulated() throws Exception {
        testListAllConsoleAware(true);
    }

    @Test
    public void testListAllContentIsNotConsolePopulated() throws Exception {
        testListAllConsoleAware(false);
    }

    protected void setUpAddExpectations() {
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
            when(httpHeaders.getRequestHeader(BackendResource.ALL_CONTENT_HEADER)).thenReturn(populates);
            setUpGetConsoleExpectations(0, 1, 2);
            setUpGetVirtioScsiExpectations(0, 1, 2);
            setUpGetSoundcardExpectations(0, 1, 2);
            setUpGetRngDeviceExpectations(0, 1, 2);
            setUpGetTpmExpectations(0, 1, 2);
        }

        setUpGetGraphicsExpectations(3);
        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    protected void verifyCollection(List<R> collection) throws Exception {
        super.verifyCollection(collection);

        List<String> populateHeader = httpHeaders.getRequestHeader(BackendResource.ALL_CONTENT_HEADER);
        boolean populated = populateHeader != null ? populateHeader.contains("true") : false;

        for (R template : collection) {
            assertTrue(populated ? template.isSetConsole() : !template.isSetConsole());
        }
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

    protected void setUpGetEntityExpectations(int index) {
        setUpGetEntityExpectations(QueryType.GetVmTemplate,
                GetVmTemplateParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[index] },
                getEntity(index));
    }

    protected void setUpGetGraphicsExpectations(int times) {
        for (int i = 0; i < times; i++) {
            setUpGetEntityExpectations(QueryType.GetGraphicsDevices,
                    IdQueryParameters.class,
                    new String[] {},
                    new Object[] {},
                    Collections.singletonList(new GraphicsDevice(VmDeviceType.SPICE)));
        }
    }

    protected abstract Response doAdd(R model);

    protected abstract R getRestModel(int index);

    protected void setUpGetTpmExpectations(int ... idxs) {
        for (int i = 0; i < idxs.length; i++) {
            setUpGetEntityExpectations(QueryType.GetTpmDevices,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[idxs[i]] },
                    new ArrayList<>());
        }
    }
}
