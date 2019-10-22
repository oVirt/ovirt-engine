/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Kernel;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Kernel;

public class V3KernelOutAdapter implements V3Adapter<Kernel, V3Kernel> {
    @Override
    public V3Kernel adapt(Kernel from) {
        V3Kernel to = new V3Kernel();
        if (from.isSetVersion()) {
            to.setVersion(adaptOut(from.getVersion()));
        }
        return to;
    }
}
