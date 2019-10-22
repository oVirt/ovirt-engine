/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.VirtioScsi;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VirtIOSCSI;

public class V3VirtioScsiOutAdapter implements V3Adapter<VirtioScsi, V3VirtIOSCSI> {
    @Override
    public V3VirtIOSCSI adapt(VirtioScsi from) {
        V3VirtIOSCSI to = new V3VirtIOSCSI();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        return to;
    }
}
