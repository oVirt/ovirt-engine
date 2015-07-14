package org.ovirt.engine.api.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.CdRom;

public interface CdRomResource extends DeviceResource<CdRom> {
    @DELETE
    Response remove();
}
