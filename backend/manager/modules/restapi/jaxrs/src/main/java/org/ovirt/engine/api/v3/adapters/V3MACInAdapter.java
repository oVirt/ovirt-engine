/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Mac;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3MAC;

public class V3MACInAdapter implements V3Adapter<V3MAC, Mac> {
    @Override
    public Mac adapt(V3MAC from) {
        Mac to = new Mac();
        if (from.isSetAddress()) {
            to.setAddress(from.getAddress());
        }
        return to;
    }
}
