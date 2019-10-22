/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.PortMirroring;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3PortMirroring;

public class V3PortMirroringOutAdapter implements V3Adapter<PortMirroring, V3PortMirroring> {
    @Override
    public V3PortMirroring adapt(PortMirroring from) {
        V3PortMirroring to = new V3PortMirroring();
        return to;
    }
}
