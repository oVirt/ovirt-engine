/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.aaa.SshPublicKeyResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3SSHPublicKey;

@Produces({"application/xml", "application/json"})
public class V3SshPublicKeyServer extends V3Server<SshPublicKeyResource> {
    public V3SshPublicKeyServer(SshPublicKeyResource delegate) {
        super(delegate);
    }

    @GET
    public V3SSHPublicKey get() {
        return adaptGet(getDelegate()::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3SSHPublicKey update(V3SSHPublicKey key) {
        return adaptUpdate(getDelegate()::update, key);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }
}
