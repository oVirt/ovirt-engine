package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Creation;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.queries.GetTasksStatusesByTasksIDsParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendCreationResourceTest
    extends AbstractBackendSubResourceTest<Creation, List/*<AsyncTaskStatus>*/, BackendCreationResource> {

    private static String CREATION_ID = GUIDS[0].toString() + "," + GUIDS[1].toString();

    public BackendCreationResourceTest() {
        super(new BackendCreationResource(CREATION_ID));
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendCreationResource("foo");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpUriInfoExpectations());
        setUpGetEntityExpectations();
        control.replay();

        verifyModel(resource.get());
    }

    protected UriInfo setUpUriInfoExpectations() {
        UriInfo uriInfo = setUpBasicUriExpectations();
        expect(uriInfo.getPath()).andReturn("").anyTimes();
        return uriInfo;
    }

    protected void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetTasksStatusesByTasksIDs,
                                   GetTasksStatusesByTasksIDsParameters.class,
                                   new String[] {},
                                   new Object[] {},
                                   getTaskStatuses());
    }

    @Override
    protected List<AsyncTaskStatus> getEntity(int index) {
        return getTaskStatuses();
    }

    protected List<AsyncTaskStatus> getTaskStatuses() {
        List<AsyncTaskStatus> ret = new ArrayList<>();
        ret.add(getTaskStatus());
        ret.add(getTaskStatus());
        return ret;
    }

    protected AsyncTaskStatus getTaskStatus() {
        AsyncTaskStatus status = new AsyncTaskStatus();
        status.setStatus(AsyncTaskStatusEnum.finished);
        status.setResult(AsyncTaskResultEnum.success);
        return status;
    }

    protected void verifyModel(Creation model) throws Exception {
        assertNotNull(model);
        assertEquals(URLEncoder.encode(CREATION_ID, "UTF-8"), model.getId());
        assertEquals(CreationStatus.COMPLETE.value(), model.getStatus());
        verifyLinks(model);
    }
}
