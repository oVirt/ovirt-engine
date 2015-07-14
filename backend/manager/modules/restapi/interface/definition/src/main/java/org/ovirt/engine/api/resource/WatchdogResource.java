package org.ovirt.engine.api.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.WatchDog;

public interface WatchdogResource extends DeviceResource<WatchDog> {
    @DELETE
    Response remove();
}
