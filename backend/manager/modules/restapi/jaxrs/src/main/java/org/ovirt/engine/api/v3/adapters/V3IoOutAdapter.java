/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Io;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3IO;

public class V3IoOutAdapter implements V3Adapter<Io, V3IO> {
    @Override
    public V3IO adapt(Io from) {
        V3IO to = new V3IO();
        if (from.isSetThreads()) {
            to.setThreads(from.getThreads());
        }
        return to;
    }
}
