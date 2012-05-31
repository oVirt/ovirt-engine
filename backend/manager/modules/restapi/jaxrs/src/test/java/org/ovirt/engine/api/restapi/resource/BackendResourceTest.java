package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.resource.BackendClustersResourceTest.setUpEntityExpectations;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.queries.GetVdsGroupByIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendResourceTest extends AbstractBackendBaseTest {
    BackendClusterResource resource;

    @Test
    public void testQueryWithoutFilter() throws Exception {
        resource.setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(false);
        control.replay();
        resource.get();
    }

    @Test
    public void testQueryWithFilter() throws Exception {
        List<String> filterValue = new ArrayList<String>();
        filterValue.add("true");
        EasyMock.reset(httpHeaders);
        expect(httpHeaders.getRequestHeader("filter")).andReturn(filterValue);
        resource.setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(true);
        control.replay();
        resource.get();
    }

    @Override
    protected void init() {
        resource = new BackendClusterResource(GUIDS[0].toString());
        resource.setBackend(backend);
        resource.setMappingLocator(mapperLocator);
        resource.setSessionHelper(sessionHelper);
        resource.setMessageBundle(messageBundle);
        resource.setHttpHeaders(httpHeaders);
    }

    protected void setUpGetEntityExpectations(boolean filter) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetVdsGroupById,
                GetVdsGroupByIdParameters.class,
                new String[] { "VdsId", "Filtered" },
                new Object[] { GUIDS[0], filter },
                getEntity(0));
    }

    protected VDSGroup getEntity(int index) {
        return setUpEntityExpectations(control.createMock(VDSGroup.class), index);
    }
}
