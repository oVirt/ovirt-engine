/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
public interface ActionResource {

    @GET
    public Response get();

    public Action getAction();
}
