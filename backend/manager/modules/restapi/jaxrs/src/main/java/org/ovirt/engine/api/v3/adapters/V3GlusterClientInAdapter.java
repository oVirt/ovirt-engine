/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.GlusterClient;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterClient;

public class V3GlusterClientInAdapter implements V3Adapter<V3GlusterClient, GlusterClient> {
    @Override
    public GlusterClient adapt(V3GlusterClient from) {
        GlusterClient to = new GlusterClient();
        if (from.isSetBytesRead()) {
            to.setBytesRead(from.getBytesRead());
        }
        if (from.isSetBytesWritten()) {
            to.setBytesWritten(from.getBytesWritten());
        }
        if (from.isSetClientPort()) {
            to.setClientPort(from.getClientPort());
        }
        if (from.isSetHostName()) {
            to.setHostName(from.getHostName());
        }
        return to;
    }
}
