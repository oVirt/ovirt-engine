/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.FileResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3File;

@Produces({"application/xml", "application/json"})
public class V3FileServer extends V3Server<FileResource> {
    public V3FileServer(FileResource delegate) {
        super(delegate);
    }

    @GET
    public V3File get() {
        return adaptGet(getDelegate()::get);
    }
}
