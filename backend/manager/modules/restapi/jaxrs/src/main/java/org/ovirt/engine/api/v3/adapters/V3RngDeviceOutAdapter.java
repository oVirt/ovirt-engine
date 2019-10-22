/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.RngDevice;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3RngDevice;

public class V3RngDeviceOutAdapter implements V3Adapter<RngDevice, V3RngDevice> {
    @Override
    public V3RngDevice adapt(RngDevice from) {
        V3RngDevice to = new V3RngDevice();
        if (from.isSetRate()) {
            to.setRate(adaptOut(from.getRate()));
        }
        if (from.isSetSource()) {
            to.setSource(from.getSource().value());
        }
        return to;
    }
}
