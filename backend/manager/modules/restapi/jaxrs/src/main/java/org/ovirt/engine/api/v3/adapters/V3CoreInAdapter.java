/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Core;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Core;

public class V3CoreInAdapter implements V3Adapter<V3Core, Core> {
    @Override
    public Core adapt(V3Core from) {
        Core to = new Core();
        if (from.isSetIndex()) {
            to.setIndex(from.getIndex());
        }
        if (from.isSetSocket()) {
            to.setSocket(from.getSocket());
        }
        return to;
    }
}
