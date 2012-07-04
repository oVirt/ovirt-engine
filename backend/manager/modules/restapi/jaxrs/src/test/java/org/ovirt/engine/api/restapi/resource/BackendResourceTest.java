package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.resource.BackendHostsResourceTest.setUpEntityExpectations;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.core.common.action.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendResourceTest extends AbstractBackendBaseTest {
    BackendHostResource resource;

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

    @Test
    public void testActionWithCorrelationId() throws Exception {
        setUpGetEntityExpectations(false);
        expect(httpHeaders.getRequestHeader("Correlation-Id")).andReturn(asList("Some-Correlation-id")).anyTimes();
        resource.setUriInfo((setUpActionExpectations(VdcActionType.UpdateVds,
                                           UpdateVdsActionParameters.class,
                                           new String[] { "RootPassword", "CorrelationId" },
                                           new Object[] { NAMES[2], "Some-Correlation-id" },
                                           true,
                                           true)));
        Action action = new Action();
        action.setRootPassword(NAMES[2]);
        resource.install(action);
    }

    @Override
    protected void init() {
        resource = new BackendHostResource(GUIDS[0].toString(), new BackendHostsResource());
        resource.setBackend(backend);
        resource.setMappingLocator(mapperLocator);
        resource.setSessionHelper(sessionHelper);
        resource.setMessageBundle(messageBundle);
        resource.setHttpHeaders(httpHeaders);
    }

    protected void setUpGetEntityExpectations(boolean filter) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetVdsByVdsId,
                GetVdsByVdsIdParameters.class,
                new String[] { "VdsId", "Filtered" },
                new Object[] { GUIDS[0], filter },
                getEntity(0));
    }

    protected VDS getEntity(int index) {
        VDS vds = setUpEntityExpectations(control.createMock(VDS.class), index);
        VdsStatic vdsStatic = control.createMock(VdsStatic.class);
        expect(vdsStatic.getId()).andReturn(GUIDS[2]).anyTimes();
        expect(vds.getStaticData()).andReturn(vdsStatic).anyTimes();
        return vds;
    }
}
