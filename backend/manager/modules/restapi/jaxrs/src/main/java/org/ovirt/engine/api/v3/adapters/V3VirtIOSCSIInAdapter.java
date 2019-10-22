/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.VirtioScsi;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VirtIOSCSI;

public class V3VirtIOSCSIInAdapter implements V3Adapter<V3VirtIOSCSI, VirtioScsi> {
    @Override
    public VirtioScsi adapt(V3VirtIOSCSI from) {
        VirtioScsi to = new VirtioScsi();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        return to;
    }
}
