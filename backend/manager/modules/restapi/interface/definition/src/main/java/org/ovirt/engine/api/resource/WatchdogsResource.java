package org.ovirt.engine.api.resource;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.WatchDog;
import org.ovirt.engine.api.model.WatchDogs;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
public interface WatchdogsResource extends DevicesResource<WatchDog, WatchDogs>{
    @Path("{id}")
    WatchdogResource getDeviceSubResource(@PathParam("id") String id);
}
