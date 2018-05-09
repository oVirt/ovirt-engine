package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.resource.BackendHostsResourceTest.ROOT_PASSWORD;
import static org.ovirt.engine.api.restapi.resource.BackendHostsResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendHostsResourceTest.setUpEntityExpectations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.restapi.utils.MalformedIdException;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.hostdeploy.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendResourceTest extends AbstractBackendBaseTest {
    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.OrganizationName, "oVirt"));
    }

    BackendHostResource resource;

    @Override
    @BeforeEach
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
    public void testQueryWithoutFilter() {
        resource.setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(false);
        resource.get();
    }

    @Test
    public void testQueryWithFilter() {
        List<String> filterValue = new ArrayList<>();
        filterValue.add("true");
        reset(httpHeaders);
        when(httpHeaders.getRequestHeader(USER_FILTER_HEADER)).thenReturn(filterValue);
        resource.setUriInfo(setUpBasicUriExpectations());
        assertThrows(WebApplicationException.class, () -> resource.get());
    }

    @Test
    public void testActionWithCorrelationId() {
        setUpGetEntityExpectations(false);
        resource.getCurrent().getParameters().put("correlation_id", "Some-Correlation-id");
        resource.setUriInfo(setUpActionExpectations(ActionType.UpdateVds,
                                           UpdateVdsActionParameters.class,
                                           new String[] { "RootPassword", "CorrelationId" },
                                           new Object[] { NAMES[2], "Some-Correlation-id" },
                                           true,
                                           true));
        Action action = new Action();
        action.setRootPassword(NAMES[2]);
        resource.install(action);
    }

    @Test
    public void testBadGuidValidation() {
        setUpGetEntityExpectations(false);
        Host host = new Host();
        host.setCluster(new Cluster());
        host.getCluster().setId("!!!");
        assertThrows(MalformedIdException.class, () -> resource.update(host));
    }

    @Override
    protected void init() {
        resource = new BackendHostResource(GUIDS[0].toString(), new BackendHostsResource());
        resource.setMappingLocator(mapperLocator);
        resource.setMessageBundle(messageBundle);
        resource.setHttpHeaders(httpHeaders);
    }

    @Test
    public void testUpdateCantDo() {
        setUpGetEntityWithNoCertificateInfoExpectations();

        resource.setUriInfo(setUpActionExpectations(ActionType.UpdateVds,
                UpdateVdsActionParameters.class,
                new String[] { "RootPassword" },
                new Object[] { ROOT_PASSWORD },
                false,
                true,
                "ACTION_TYPE_FAILED_IMPORT_DISKS_ALREADY_EXIST"));

        verifyFault(
                assertThrows(WebApplicationException.class, () -> resource.update(getModel(0))),
                "ACTION_TYPE_FAILED_IMPORT_DISKS_ALREADY_EXIST", 409);
    }

    private void setUpGetEntityWithNoCertificateInfoExpectations() {
        setUpGetEntityWithNoCertificateInfoExpectations(1, false, getEntity(0));
    }

    private void setUpGetEntityWithNoCertificateInfoExpectations(int times, boolean notFound, VDS entity) {
        while (times-- > 0) {
            setUpGetEntityExpectations(QueryType.GetVdsByVdsId,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[0] },
                    notFound ? null : entity);
        }
    }

    protected void setUpGetEntityExpectations(boolean filter) {
        setUpGetEntityExpectations(QueryType.GetVdsByVdsId,
                IdQueryParameters.class,
                new String[] { "Id", "Filtered" },
                new Object[] { GUIDS[0], filter },
                getEntity(0));
    }



    protected VDS getEntity(int index) {
        return setUpEntityExpectations(spy(new VDS()), null, index);
    }
}
