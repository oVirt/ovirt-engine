/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.RngDevice;
import org.ovirt.engine.api.model.RngSource;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3RngDevice;

public class V3RngDeviceInAdapter implements V3Adapter<V3RngDevice, RngDevice> {
    @Override
    public RngDevice adapt(V3RngDevice from) {
        RngDevice to = new RngDevice();
        if (from.isSetRate()) {
            to.setRate(adaptIn(from.getRate()));
        }
        if (from.isSetSource()) {
            to.setSource(RngSource.fromValue(from.getSource()));
        }
        return to;
    }
}
