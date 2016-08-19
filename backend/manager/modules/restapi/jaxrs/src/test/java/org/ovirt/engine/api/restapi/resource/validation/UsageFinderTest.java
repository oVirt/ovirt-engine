package org.ovirt.engine.api.restapi.resource.validation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.api.model.Fault;

public class UsageFinderTest extends Assert {

    private UsageFinder usageFinder;

    @Before
    public void init() {
        usageFinder = new UsageFinder();
    }

    @Test
    public void testAdd() {
        try {
            UriInfo uriInfo = mockUri("hosts", "00000001-0001-0001-0001-000000000011", "nics");
            Request request = mockRequest("POST");
            Fault fault = usageFinder.getUsageMessage(uriInfo, request);
            assertEquals("For correct usage, see: http://localhost:8080/ovirt-engine/api/model#services/host-nics/methods/add", fault.getDetail());
        } catch (ClassNotFoundException | IOException | URISyntaxException e) {
            fail();
        }
    }

    @Test
    public void testAction() {
        try {
            UriInfo uriInfo = mockUri("vms", "00000001-0001-0001-0001-000000000011", "freezefilesystems");
            Request request = mockRequest("POST");
            Fault fault = usageFinder.getUsageMessage(uriInfo, request);
            assertEquals( "For correct usage, see: http://localhost:8080/ovirt-engine/api/model#services/vm/methods/freeze-filesystems", fault.getDetail());
        } catch (URISyntaxException | ClassNotFoundException | IOException e) {
            fail();
        }
    }

    @Test
    public void testUpdateWithNonGuidId() {
        try {
            UriInfo uriInfo = mockUri("hosts", "00000001-0001-0001-0001-000000000011", "nics", "116"); //LUN id.
            Request request = mockRequest("PUT");
            Fault fault = usageFinder.getUsageMessage(uriInfo, request);
            assertEquals("For correct usage, see: http://localhost:8080/ovirt-engine/api/model#services/host-nic/methods/update", fault.getDetail());
        } catch (URISyntaxException | ClassNotFoundException | IOException e) {
            fail();
        }
    }

    private Request mockRequest(String httpMethod) {
        Request requestMock = mock(Request.class);
        when(requestMock.getMethod()).thenReturn(httpMethod);
        return requestMock;
    }

    private UriInfo mockUri(String...strings) throws URISyntaxException {
        UriInfo uriInfoMock = mock(UriInfo.class);
        when(uriInfoMock.getBaseUri()).thenReturn(new URI("http://localhost:8080/ovirt-engine/api/"));
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
