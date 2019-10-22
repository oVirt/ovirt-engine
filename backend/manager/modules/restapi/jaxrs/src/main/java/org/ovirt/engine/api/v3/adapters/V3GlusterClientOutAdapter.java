/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.GlusterClient;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterClient;

public class V3GlusterClientOutAdapter implements V3Adapter<GlusterClient, V3GlusterClient> {
    @Override
    public V3GlusterClient adapt(GlusterClient from) {
        V3GlusterClient to = new V3GlusterClient();
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
