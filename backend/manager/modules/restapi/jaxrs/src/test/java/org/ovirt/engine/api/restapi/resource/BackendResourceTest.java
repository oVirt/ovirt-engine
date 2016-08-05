package org.ovirt.engine.api.restapi.resource;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.resource.BackendHostsResourceTest.ROOT_PASSWORD;
import static org.ovirt.engine.api.restapi.resource.BackendHostsResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendHostsResourceTest.setUpEntityExpectations;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.Rule;
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
import org.ovirt.engine.core.utils.MockConfigRule;

public class BackendResourceTest extends AbstractBackendBaseTest {
    @Rule
    public MockConfigRule mcr = new MockConfigRule();

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
        resource.get();
    }

    @Test(expected = javax.ws.rs.WebApplicationException.class)
    public void testQueryWithFilter() throws Exception {
        List<String> filterValue = new ArrayList<>();
        filterValue.add("true");
        reset(httpHeaders);
        when(httpHeaders.getRequestHeader(USER_FILTER_HEADER)).thenReturn(filterValue);
        resource.setUriInfo(setUpBasicUriExpectations());
        resource.get();
    }

    @Test
    public void testActionWithCorrelationId() throws Exception {
        setUpGetEntityExpectations(false);
        resource.getCurrent().getParameters().put("correlation_id", "Some-Correlation-id");
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
        return setUpEntityExpectations(spy(new VDS()), null, index);
    }
}
