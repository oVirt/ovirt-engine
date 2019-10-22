/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.resource;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

public interface AsynchronouslyCreatedResource {

    @Path("creation_status/{oid}")
    public CreationResource getCreationResource(@PathParam("oid") String oid);

}
