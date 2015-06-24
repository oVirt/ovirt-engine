package org.ovirt.engine.api.resource;

import org.ovirt.engine.api.model.CdRom;

import javax.ws.rs.DELETE;
import javax.ws.rs.core.Response;

public interface CdRomResource extends DeviceResource<CdRom> {
    @DELETE
    Response remove();
}
