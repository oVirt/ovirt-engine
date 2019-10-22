/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Usb;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Usb;

public class V3UsbOutAdapter implements V3Adapter<Usb, V3Usb> {
    @Override
    public V3Usb adapt(Usb from) {
        V3Usb to = new V3Usb();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        if (from.isSetType()) {
            to.setType(from.getType().value());
        }
        return to;
    }
}
