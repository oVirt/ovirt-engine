/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.ExternalProviderCertificateResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Certificate;

@Produces({"application/xml", "application/json"})
public class V3ExternalProviderCertificateServer extends V3Server<ExternalProviderCertificateResource> {
    public V3ExternalProviderCertificateServer(ExternalProviderCertificateResource delegate) {
        super(delegate);
    }

    @GET
    public V3Certificate get() {
        return adaptGet(getDelegate()::get);
    }
}
