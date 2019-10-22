/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Kernel;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Kernel;

public class V3KernelInAdapter implements V3Adapter<V3Kernel, Kernel> {
    @Override
    public Kernel adapt(V3Kernel from) {
        Kernel to = new Kernel();
        if (from.isSetVersion()) {
            to.setVersion(adaptIn(from.getVersion()));
        }
        return to;
    }
}
