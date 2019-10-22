/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Core;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Core;

public class V3CoreOutAdapter implements V3Adapter<Core, V3Core> {
    @Override
    public V3Core adapt(Core from) {
        V3Core to = new V3Core();
        if (from.isSetIndex()) {
            to.setIndex(from.getIndex());
        }
        if (from.isSetSocket()) {
            to.setSocket(from.getSocket());
        }
        return to;
    }
}
