package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.resource.BackendHostsResourceTest.ROOT_PASSWORD;
import static org.ovirt.engine.api.restapi.resource.BackendHostsResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendHostsResourceTest.setUpEntityExpectations;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.easymock.EasyMock;
import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.restapi.utils.MalformedIdException;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.hostdeploy.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendResourceTest extends AbstractBackendBaseTest {
    BackendHostResource resource;

    @Override
    public void setUp() {
        super.setUp();
        setUpParentMock(resource.getParent());
    }

    private void setUpParentMock(BackendHostsResource parent) {
        parent.setMappingLocator(mapperLocator);
        parent.setMessageBundle(messageBundle);
        parent.setHttpHeaders(httpHeaders);
    }

    @Test
    public void testQueryWithoutFilter() throws Exception {
        resource.setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(false);
        control.replay();
        resource.get();
    }

    @Test(expected = javax.ws.rs.WebApplicationException.class)
    public void testQueryWithFilter() throws Exception {
        List<String> filterValue = new ArrayList<>();
        filterValue.add("true");
        EasyMock.reset(httpHeaders);
        expect(httpHeaders.getRequestHeader(USER_FILTER_HEADER)).andReturn(filterValue);
        resource.setUriInfo(setUpBasicUriExpectations());
        control.replay();
        resource.get();
    }

    @Test
    public void testActionWithCorrelationId() throws Exception {
        setUpGetEntityExpectations(false);
        expect(httpHeaders.getRequestHeader("Correlation-Id")).andReturn(asList("Some-Correlation-id")).anyTimes();
        resource.setUriInfo(setUpActionExpectations(VdcActionType.UpdateVds,
                                           UpdateVdsActionParameters.class,
                                           new String[] { "RootPassword", "CorrelationId" },
                                           new Object[] { NAMES[2], "Some-Correlation-id" },
                                           true,
                                           true));
        Action action = new Action();
        action.setRootPassword(NAMES[2]);
        resource.install(action);
    }

    @Test(expected = MalformedIdException.class)
    public void testBadGuidValidation() throws Exception {
        setUpGetEntityExpectations(false);
        Host host = new Host();
        host.setCluster(new Cluster());
        host.getCluster().setId("!!!");
        control.replay();
        resource.update(host);
    }

    @Override
    protected void init() {
        resource = new BackendHostResource(GUIDS[0].toString(), new BackendHostsResource());
        resource.setMappingLocator(mapperLocator);
        resource.setMessageBundle(messageBundle);
        resource.setHttpHeaders(httpHeaders);
    }

    @Test
    public void testUpdateCantDo() throws Exception {
        setUpGetEntityWithNoCertificateInfoExpectations();

        resource.setUriInfo(setUpActionExpectations(VdcActionType.UpdateVds,
                UpdateVdsActionParameters.class,
                new String[] { "RootPassword" },
                new Object[] { ROOT_PASSWORD },
                false,
                true,
                "ACTION_TYPE_FAILED_IMPORT_DISKS_ALREADY_EXIST"));

        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, "ACTION_TYPE_FAILED_IMPORT_DISKS_ALREADY_EXIST", 409);
        }
    }

    private void setUpGetEntityWithNoCertificateInfoExpectations() throws Exception {
        setUpGetEntityWithNoCertificateInfoExpectations(1, false, getEntity(0));
    }

    private void setUpGetEntityWithNoCertificateInfoExpectations(int times, boolean notFound, VDS entity)
            throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetVdsByVdsId,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[0] },
                    notFound ? null : entity);
        }
    }

    protected void setUpGetEntityExpectations(boolean filter) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetVdsByVdsId,
                IdQueryParameters.class,
                new String[] { "Id", "Filtered" },
                new Object[] { GUIDS[0], filter },
                getEntity(0));
    }



    protected VDS getEntity(int index) {
        VDS vds = setUpEntityExpectations(control.createMock(VDS.class), null, index);
        return vds;
    }
}
