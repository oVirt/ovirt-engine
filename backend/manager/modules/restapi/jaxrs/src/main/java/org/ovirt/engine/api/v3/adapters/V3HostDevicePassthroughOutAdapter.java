/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.HostDevicePassthrough;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3HostDevicePassthrough;

public class V3HostDevicePassthroughOutAdapter implements V3Adapter<HostDevicePassthrough, V3HostDevicePassthrough> {
    @Override
    public V3HostDevicePassthrough adapt(HostDevicePassthrough from) {
        V3HostDevicePassthrough to = new V3HostDevicePassthrough();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        return to;
    }
}
