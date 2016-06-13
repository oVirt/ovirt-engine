package org.ovirt.engine.api.restapi.resource.validation;

import static org.easymock.EasyMock.expect;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.api.model.Fault;

public class UsageFinderTest extends Assert {

    private UsageFinder usageFinder;
    private IMocksControl control;

    @Before
    public void init() {
        usageFinder = new UsageFinder();
        control = EasyMock.createNiceControl();
    }

    @After
    public void tearDown() {
        control.verify();
    }

    @Test
    public void testAdd() {
        try {
            UriInfo uriInfo = mockUri("hosts", "00000001-0001-0001-0001-000000000011", "nics");
            Request request = mockRequest("POST");
            control.replay();
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
            control.replay();
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
            control.replay();
            Fault fault = usageFinder.getUsageMessage(uriInfo, request);
            assertEquals("For correct usage, see: http://localhost:8080/ovirt-engine/api/model#services/host-nic/methods/update", fault.getDetail());
        } catch (URISyntaxException | ClassNotFoundException | IOException e) {
            fail();
        }
    }

    private Request mockRequest(String httpMethod) {
        Request requestMock = control.createMock(Request.class);
        expect(requestMock.getMethod()).andReturn(httpMethod);
        return requestMock;
    }

    private UriInfo mockUri(String...strings) throws URISyntaxException {
        UriInfo uriInfoMock = control.createMock(UriInfo.class);
        expect(uriInfoMock.getBaseUri()).andReturn(new URI("http://localhost:8080/ovirt-engine/api/"));
        List<PathSegment> pathSegments = new ArrayList<>();
        for (String s : strings) {
            PathSegment segment = control.createMock(PathSegment.class);
            expect(segment.getPath()).andReturn(s).anyTimes();
            pathSegments.add(segment);
        }
        expect(uriInfoMock.getPathSegments()).andReturn(pathSegments);
        return uriInfoMock;
    }
}
