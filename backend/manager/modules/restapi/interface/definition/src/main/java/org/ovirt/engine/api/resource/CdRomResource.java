package org.ovirt.engine.api.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Cdrom;

public interface CdRomResource extends DeviceResource<Cdrom> {
    @DELETE
    Response remove();
}
