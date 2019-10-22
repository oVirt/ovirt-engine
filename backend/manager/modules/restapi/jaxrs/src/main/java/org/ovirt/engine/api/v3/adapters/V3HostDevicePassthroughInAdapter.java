/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.HostDevicePassthrough;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3HostDevicePassthrough;

public class V3HostDevicePassthroughInAdapter implements V3Adapter<V3HostDevicePassthrough, HostDevicePassthrough> {
    @Override
    public HostDevicePassthrough adapt(V3HostDevicePassthrough from) {
        HostDevicePassthrough to = new HostDevicePassthrough();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        return to;
    }
}
