/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Mac;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3MAC;

public class V3MacOutAdapter implements V3Adapter<Mac, V3MAC> {
    @Override
    public V3MAC adapt(Mac from) {
        V3MAC to = new V3MAC();
        if (from.isSetAddress()) {
            to.setAddress(from.getAddress());
        }
        return to;
    }
}
