package org.ovirt.engine.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.ReportedDevices;

@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface VmReportedDevicesResource {

    @GET
    public ReportedDevices list();

    /**
     * Sub-resource locator method, returns individual VmReportedDeviceResource on which the remainder of the URI is
     * dispatched.
     * @param id
     *            the NetworkDevice ID
     * @return matching subresource if found
     */
    @Path("{id}")
    public VmReportedDeviceResource getVmReportedDeviceSubResource(@PathParam("id") String id);
}
