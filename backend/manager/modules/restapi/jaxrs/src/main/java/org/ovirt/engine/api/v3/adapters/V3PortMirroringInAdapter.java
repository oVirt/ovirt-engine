/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.PortMirroring;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3PortMirroring;

public class V3PortMirroringInAdapter implements V3Adapter<V3PortMirroring, PortMirroring> {
    @Override
    public PortMirroring adapt(V3PortMirroring from) {
        PortMirroring to = new PortMirroring();
        return to;
    }
}
