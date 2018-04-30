package org.ovirt.engine.api.restapi.resource.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.restapi.invocation.Current;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;

public class UsageFinderTest {

    private UsageFinder usageFinder;

    @BeforeEach
    public void init() {
        Current current = new Current();
        current.setRoot("http://localhost:8080");
        current.setPrefix("/ovirt-engine/api");
        CurrentManager.put(current);
        usageFinder = new UsageFinder();
    }

    @Test
    public void testAdd() throws Exception {
        UriInfo uriInfo = mockUri("hosts", "00000001-0001-0001-0001-000000000011", "nics");
        Request request = mockRequest("POST");
        Fault fault = usageFinder.getUsageMessage(uriInfo, request);
        assertEquals("For correct usage, see: http://localhost:8080/ovirt-engine/apidoc#services/host_nics/methods/add", fault.getDetail());
    }

    @Test
    public void testAction() throws Exception {
        UriInfo uriInfo = mockUri("vms", "00000001-0001-0001-0001-000000000011", "freezefilesystems");
        Request request = mockRequest("POST");
        Fault fault = usageFinder.getUsageMessage(uriInfo, request);
        assertEquals( "For correct usage, see: http://localhost:8080/ovirt-engine/apidoc#services/vm/methods/freeze_filesystems", fault.getDetail());
    }

    @Test
    public void testUpdateWithNonGuidId() throws Exception {
        UriInfo uriInfo = mockUri("hosts", "00000001-0001-0001-0001-000000000011", "nics", "116"); //LUN id.
        Request request = mockRequest("PUT");
        Fault fault = usageFinder.getUsageMessage(uriInfo, request);
        assertEquals("For correct usage, see: http://localhost:8080/ovirt-engine/apidoc#services/host_nic/methods/update", fault.getDetail());
    }

    private Request mockRequest(String httpMethod) {
        Request requestMock = mock(Request.class);
        when(requestMock.getMethod()).thenReturn(httpMethod);
        return requestMock;
    }

    private UriInfo mockUri(String...strings) {
        UriInfo uriInfoMock = mock(UriInfo.class);
        List<PathSegment> pathSegments = new ArrayList<>();
        for (String s : strings) {
            PathSegment segment = mock(PathSegment.class);
            when(segment.getPath()).thenReturn(s);
            pathSegments.add(segment);
        }
        when(uriInfoMock.getPathSegments()).thenReturn(pathSegments);
        return uriInfoMock;
    }
}
