/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.StorageResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Storage;

@Produces({"application/xml", "application/json"})
public class V3StorageServer extends V3Server<StorageResource> {
    public V3StorageServer(StorageResource delegate) {
        super(delegate);
    }

    @GET
    public V3Storage get() {
        return adaptGet(getDelegate()::get);
    }
}
