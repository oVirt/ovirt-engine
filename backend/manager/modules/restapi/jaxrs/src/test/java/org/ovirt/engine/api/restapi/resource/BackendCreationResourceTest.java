package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Creation;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.queries.GetTasksStatusesByTasksIDsParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendCreationResourceTest
    extends AbstractBackendSubResourceTest<Creation, List/*<AsyncTaskStatus>*/, BackendCreationResource> {

    private static String CREATION_ID = GUIDS[0].toString() + "," + GUIDS[1].toString();

    public BackendCreationResourceTest() {
        super(new BackendCreationResource(CREATION_ID));
    }

    @Test
    public void testBadGuid() {
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> new BackendCreationResource("foo")));
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpUriInfoExpectations());
        setUpGetEntityExpectations();

        verifyModel(resource.get());
    }

    protected UriInfo setUpUriInfoExpectations() {
        UriInfo uriInfo = setUpBasicUriExpectations();
        when(uriInfo.getPath()).thenReturn("");
        return uriInfo;
    }

    protected void setUpGetEntityExpectations() {
        setUpGetEntityExpectations(QueryType.GetTasksStatusesByTasksIDs,
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
