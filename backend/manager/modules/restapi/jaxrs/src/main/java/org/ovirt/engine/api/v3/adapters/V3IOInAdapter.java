/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Io;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3IO;

public class V3IOInAdapter implements V3Adapter<V3IO, Io> {
    @Override
    public Io adapt(V3IO from) {
        Io to = new Io();
        if (from.isSetThreads()) {
            to.setThreads(from.getThreads());
        }
        return to;
    }
}
