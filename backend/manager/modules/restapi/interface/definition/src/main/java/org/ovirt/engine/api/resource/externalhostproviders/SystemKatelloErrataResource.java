package org.ovirt.engine.api.resource.externalhostproviders;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.ApiMediaType;

@Path("/katelloerrata")
@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
public interface SystemKatelloErrataResource extends KatelloErrataResource {
    // This interface doesn't add any new methods, it is just a placeholder for the annotation that specifies the path
    // of the resource that manages the katello errata assigned to the system object.
}
