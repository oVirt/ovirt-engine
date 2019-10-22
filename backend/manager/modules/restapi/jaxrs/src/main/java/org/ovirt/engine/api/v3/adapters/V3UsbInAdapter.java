/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Usb;
import org.ovirt.engine.api.model.UsbType;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Usb;

public class V3UsbInAdapter implements V3Adapter<V3Usb, Usb> {
    @Override
    public Usb adapt(V3Usb from) {
        Usb to = new Usb();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        if (from.isSetType()) {
            to.setType(UsbType.fromValue(from.getType()));
        }
        return to;
    }
}
